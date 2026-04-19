package com.example.version.repository
import com.example.version.models.Post
import com.example.version.models.User
import com.example.version.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PhotoRepository {

    // ============================================
    // GET SINGLE POST BY ID (Real-time listener)
    // ============================================
    override suspend fun getPostById(postId: String): Flow<Resource<Post>> = callbackFlow {
        // Initial loading state
        trySend(Resource.Loading)

        try {
            // Add real-time listener to post document
            val listener = firestore
                .collection("posts")
                .document(postId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Error occurred
                        trySend(Resource.Error(error.message ?: "Failed to fetch post"))
                        return@addSnapshotListener
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        // Post doesn't exist
                        trySend(Resource.Error("Post not found"))
                        return@addSnapshotListener
                    }

                    // Convert snapshot to Post object
                    val post = snapshot.toObject(Post::class.java)
                    if (post != null) {
                        trySend(Resource.Success(post))
                    } else {
                        trySend(Resource.Error("Failed to parse post data"))
                    }
                }

            // Remove listener when flow closes
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // ============================================
    // GET COMMENTS FOR A POST (Real-time)
    // ============================================
    override suspend fun getCommentsByPostId(postId: String): Flow<Resource<List<Map<String, Any>>>> = callbackFlow {
        trySend(Resource.Loading)

        try {
            // Query comments subcollection ordered by date (newest first)
            val listener = firestore
                .collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("commentDatetime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(error.message ?: "Failed to fetch comments"))
                        return@addSnapshotListener
                    }

                    // Convert all comment documents to maps
                    val comments = snapshot?.documents?.mapNotNull { doc ->
                        doc.data  // Returns as Map<String, Any>
                    } ?: emptyList()

                    trySend(Resource.Success(comments))
                }

            // Remove listener when flow closes
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // ============================================
    // GET LIKES COUNT FOR POST (Real-time)
    // ============================================
    override suspend fun getPostLikesCount(postId: String): Flow<Int> = callbackFlow {
        try {
            // Listen to post document for likesCount field
            val listener = firestore
                .collection("posts")
                .document(postId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(0)
                        return@addSnapshotListener
                    }

                    // Get likesCount field from post
                    val likesCount = snapshot?.getLong("likesCount")?.toInt() ?: 0
                    trySend(likesCount)
                }

            // Remove listener when flow closes
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            trySend(0)
        }
    }

    // ============================================
    // CHECK IF USER LIKED POST
    // ============================================
    override suspend fun isPostLikedByUser(
        postId: String,
        userId: String
    ): Boolean {
        return try {
            // Check if like document exists
            val likeDoc = firestore
                .collection("posts")
                .document(postId)
                .collection("likes")
                .document(userId)
                .get()
                .await()

            // Return true if exists, false otherwise
            likeDoc.exists()
        } catch (e: Exception) {
            false
        }
    }

    // ============================================
    // TOGGLE LIKE (Like/Unlike)
    // ============================================
    override suspend fun toggleLike(
        postId: String,
        userId: String
    ): Resource<Boolean> {
        return try {
            // Step 1: Check if user already liked
            val likeDoc = firestore
                .collection("posts")
                .document(postId)
                .collection("likes")
                .document(userId)
                .get()
                .await()

            val isCurrentlyLiked = likeDoc.exists()

            if (isCurrentlyLiked) {
                // UNLIKE: Delete like document
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("likes")
                    .document(userId)
                    .delete()
                    .await()

                // Decrement like count
                firestore
                    .collection("posts")
                    .document(postId)
                    .update("likesCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()

                // Return false (now unlike)
                Resource.Success(false)
            } else {
                // LIKE: Create like document
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("likes")
                    .document(userId)
                    .set(mapOf(
                        "userId" to userId,
                        "likedAt" to Timestamp.now()
                    ))
                    .await()

                // Increment like count
                firestore
                    .collection("posts")
                    .document(postId)
                    .update("likesCount", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()

                // Return true (now liked)
                Resource.Success(true)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to toggle like")
        }
    }

    // ============================================
    // GET USER BY ID (For post creator info)
    // ============================================
    override suspend fun getUserById(userId: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading)

        try {
            // Add listener to user document
            val listener = firestore
                .collection("users")
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

                    // Convert to User object
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        trySend(Resource.Success(user))
                    } else {
                        trySend(Resource.Error("Failed to parse user data"))
                    }
                }

            // Remove listener when flow closes
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
}