package com.example.version.repository

import com.example.version.models.Post
import com.example.version.models.User
import com.example.version.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PhotoRepository {

    override suspend fun getPostById(postId: String): Flow<Resource<Post>> = callbackFlow {
        trySend(Resource.Loading)

        var registration: ListenerRegistration? = null

        try {
            registration = firestore.collection("posts")
                .document(postId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(error.message ?: "Failed to fetch post"))
                        return@addSnapshotListener
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        trySend(Resource.Error("Post not found"))
                        return@addSnapshotListener
                    }

                    val post = snapshot.toObject(Post::class.java)
                    if (post != null) trySend(Resource.Success(post))
                    else trySend(Resource.Error("Failed to parse post data"))
                }
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Unknown error occurred"))
        }

        // ✅ always last, always executed
        awaitClose { registration?.remove() }
    }

    override suspend fun getCommentsByPostId(
        postId: String
    ): Flow<Resource<List<Map<String, Any>>>> = callbackFlow {
        trySend(Resource.Loading)

        var registration: ListenerRegistration? = null

        try {
            registration = firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("commentDatetime", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(error.message ?: "Failed to fetch comments"))
                        return@addSnapshotListener
                    }

                    val comments = snapshot?.documents?.mapNotNull { it.data }.orEmpty()
                    trySend(Resource.Success(comments))
                }
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Unknown error occurred"))
        }

        awaitClose { registration?.remove() }
    }

    override suspend fun getPostLikesCount(postId: String): Flow<Int> = callbackFlow {
        var registration: ListenerRegistration? = null

        try {
            registration = firestore.collection("posts")
                .document(postId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(0)
                        return@addSnapshotListener
                    }
                    val likesCount = snapshot?.getLong("likesCount")?.toInt() ?: 0
                    trySend(likesCount)
                }
        } catch (_: Exception) {
            // ignore, will emit nothing else
        }

        awaitClose { registration?.remove() }
    }

    override suspend fun isPostLikedByUser(postId: String, userId: String): Boolean {
        return try {
            val likeDoc = firestore.collection("posts")
                .document(postId)
                .collection("likes")
                .document(userId)
                .get()
                .await()
            likeDoc.exists()
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun toggleLike(postId: String, userId: String): Resource<Boolean> {
        return try {
            val likeDoc = firestore.collection("posts")
                .document(postId)
                .collection("likes")
                .document(userId)
                .get()
                .await()

            val isCurrentlyLiked = likeDoc.exists()

            if (isCurrentlyLiked) {
                firestore.collection("posts").document(postId)
                    .collection("likes").document(userId).delete().await()

                firestore.collection("posts").document(postId)
                    .update("likesCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()

                Resource.Success(false)
            } else {
                firestore.collection("posts").document(postId)
                    .collection("likes").document(userId)
                    .set(mapOf("userId" to userId, "likedAt" to Timestamp.now()))
                    .await()

                firestore.collection("posts").document(postId)
                    .update("likesCount", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()

                Resource.Success(true)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to toggle like")
        }
    }

    override suspend fun getUserById(userId: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading)

        var registration: ListenerRegistration? = null

        try {
            registration = firestore.collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(error.message ?: "Failed to fetch user"))
                        return@addSnapshotListener
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        trySend(Resource.Error("User not found"))
                        return@addSnapshotListener
                    }

                    val user = snapshot.toObject(User::class.java)
                    if (user != null) trySend(Resource.Success(user))
                    else trySend(Resource.Error("Failed to parse user data"))
                }
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Unknown error occurred"))
        }

        awaitClose { registration?.remove() }
    }
}