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
                // ✅ UNLIKE
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("likes")
                    .document(userId)
                    .delete()
                    .await()

                firestore
                    .collection("posts")
                    .document(postId)
                    .update("likesCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()

                Log.d("LikeRepo", "❌ Post unliked: $postId by $userId")
                Resource.Success(false)
            } else {
                // ✅ LIKE
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("likes")
                    .document(userId)
                    .set(
                        mapOf(
                            "userId" to userId,
                            "likedAt" to com.google.firebase.Timestamp.now()
                        )
                    )
                    .await()

                firestore
                    .collection("posts")
                    .document(postId)
                    .update("likesCount", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()

                // ✅ Backend (Cloud Functions) will create notification + send push
                Log.d("LikeRepo", "✅ Post liked. Notification will be handled by backend.")

                Resource.Success(true)
            }
        } catch (e: Exception) {
            Log.e("LikeRepo", "❌ Failed to toggle post like: ${e.message}")
            Resource.Error(e.message ?: "Failed to toggle like")
        }
    }

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
                trySend(likesCount)
            }

        awaitClose { listener.remove() }
    }

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

                trySend(userIds)
            }

        awaitClose { listener.remove() }
    }

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
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(commentId)
                    .collection("likes")
                    .document(userId)
                    .delete()
                    .await()

                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(commentId)
                    .update("likeCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()

                Resource.Success(false)
            } else {
                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(commentId)
                    .collection("likes")
                    .document(userId)
                    .set(
                        mapOf(
                            "userId" to userId,
                            "likedAt" to com.google.firebase.Timestamp.now()
                        )
                    )
                    .await()

                firestore
                    .collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(commentId)
                    .update("likeCount", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()

                Resource.Success(true)
            }
        } catch (e: Exception) {
            Log.e("LikeRepo", "Failed to toggle comment like: ${e.message}")
            Resource.Error(e.message ?: "Failed to toggle comment like")
        }
    }

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