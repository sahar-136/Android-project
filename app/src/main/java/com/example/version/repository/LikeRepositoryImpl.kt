package com.example.version.repository

import android.util.Log
import com.example.version.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LikeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : LikeRepository {

    // ============================================
    // TOGGLE LIKE ON POST (Like/Unlike)
    // ============================================
    override suspend fun togglePostLike(
        postId: String,
        userId: String
    ): Resource<Boolean> {
        return try {
            val likeDoc = firestore
                .collection("posts")
                .document(postId)
                .collection("likes")
                .document(userId)
                .get()
                .await()

            val isCurrentlyLiked = likeDoc.exists()

            if (isCurrentlyLiked) {
                // Unlike: Delete like document
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("likes")
                    .document(userId)
                    .delete()
                    .await()

                // Decrement likesCount in post
                firestore
                    .collection("posts")
                    .document(postId)
                    .update(
                        "likesCount",
                        com.google.firebase.firestore.FieldValue.increment(-1)
                    )
                    .await()

                Log.d("LikeRepo", "Post unliked: $postId by $userId")
                Resource.Success(false) // false = unliked
            } else {
                // Like: Create like document
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("likes")
                    .document(userId)
                    .set(mapOf(
                        "userId" to userId,
                        "likedAt" to com.google.firebase.Timestamp.now()
                    ))
                    .await()

                // Increment likesCount in post
                firestore
                    .collection("posts")
                    .document(postId)
                    .update(
                        "likesCount",
                        com.google.firebase.firestore.FieldValue.increment(1)
                    )
                    .await()

                Log.d("LikeRepo", "Post liked: $postId by $userId")
                Resource.Success(true) // true = liked
            }
        } catch (e: Exception) {
            Log.e("LikeRepo", "Failed to toggle post like: ${e.message}")
            Resource.Error(e.message ?: "Failed to toggle like")
        }
    }

    // ============================================
    // CHECK IF POST IS LIKED BY USER
    // ============================================
    override suspend fun isPostLikedByUser(
        postId: String,
        userId: String
    ): Boolean {
        return try {
            val likeDoc = firestore
                .collection("posts")
                .document(postId)
                .collection("likes")
                .document(userId)
                .get()
                .await()

            likeDoc.exists()
        } catch (e: Exception) {
            Log.e("LikeRepo", "Failed to check if post liked: ${e.message}")
            false
        }
    }

    // ============================================
    // GET POST LIKES COUNT (Real-time)
    // ============================================
    override suspend fun getPostLikesCount(postId: String): Flow<Int> = callbackFlow {
        val listener = firestore
            .collection("posts")
            .document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("LikeRepo", "Error fetching likes count: ${error.message}")
                    trySend(0)
                    return@addSnapshotListener
                }

                val likesCount = snapshot?.getLong("likesCount")?.toInt() ?: 0
                Log.d("LikeRepo", "Fetched likes count for $postId: $likesCount")
                trySend(likesCount)
            }

        awaitClose { listener.remove() }
    }

    // ============================================
    // GET ALL USERS WHO LIKED POST
    // ============================================
    override suspend fun getPostLikes(postId: String): Flow<List<String>> = callbackFlow {
        val listener = firestore
            .collection("posts")
            .document(postId)
            .collection("likes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("LikeRepo", "Error fetching post likes: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val userIds = snapshot?.documents?.mapNotNull { doc ->
                    doc.getString("userId")
                } ?: emptyList()

                Log.d("LikeRepo", "Fetched ${userIds.size} likes for post: $postId")
                trySend(userIds)
            }

        awaitClose { listener.remove() }
    }

    // ============================================
    // TOGGLE LIKE ON COMMENT (Like/Unlike)
    // ============================================
    override suspend fun toggleCommentLike(
        postId: String,
        commentId: String,
        userId: String
    ): Resource<Boolean> {
        return try {
            val likeDoc = firestore
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId)
                .collection("likes")
                .document(userId)
                .get()
                .await()

            val isCurrentlyLiked = likeDoc.exists()

            if (isCurrentlyLiked) {
                // Unlike: Delete like document
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(commentId)
                    .collection("likes")
                    .document(userId)
                    .delete()
                    .await()

                // Decrement likeCount in comment
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(commentId)
                    .update(
                        "likeCount",
                        com.google.firebase.firestore.FieldValue.increment(-1)
                    )
                    .await()

                Log.d("LikeRepo", "Comment unliked: $commentId by $userId")
                Resource.Success(false) // false = unliked
            } else {
                // Like: Create like document
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(commentId)
                    .collection("likes")
                    .document(userId)
                    .set(mapOf(
                        "userId" to userId,
                        "likedAt" to com.google.firebase.Timestamp.now()
                    ))
                    .await()

                // Increment likeCount in comment
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(commentId)
                    .update(
                        "likeCount",
                        com.google.firebase.firestore.FieldValue.increment(1)
                    )
                    .await()

                Log.d("LikeRepo", "Comment liked: $commentId by $userId")
                Resource.Success(true) // true = liked
            }
        } catch (e: Exception) {
            Log.e("LikeRepo", "Failed to toggle comment like: ${e.message}")
            Resource.Error(e.message ?: "Failed to toggle comment like")
        }
    }

    // ============================================
    // CHECK IF COMMENT IS LIKED BY USER
    // ============================================
    override suspend fun isCommentLikedByUser(
        postId: String,
        commentId: String,
        userId: String
    ): Boolean {
        return try {
            val likeDoc = firestore
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId)
                .collection("likes")
                .document(userId)
                .get()
                .await()

            likeDoc.exists()
        } catch (e: Exception) {
            Log.e("LikeRepo", "Failed to check if comment liked: ${e.message}")
            false
        }
    }
}