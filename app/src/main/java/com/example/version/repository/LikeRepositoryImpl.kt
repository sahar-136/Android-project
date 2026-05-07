package com.example.version.repository

import android.util.Log
import com.example.version.util.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LikeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : LikeRepository {

    override suspend fun togglePostLike(postId: String, userId: String): Resource<Boolean> {
        return try {
            val postRef = firestore.collection("posts").document(postId)
            val likeRef = postRef.collection("likes").document(userId)

            val result = firestore.runTransaction { tx ->
                val likeSnap = tx.get(likeRef)
                val isCurrentlyLiked = likeSnap.exists()

                if (isCurrentlyLiked) {
                    // UNLIKE
                    tx.delete(likeRef)
                    tx.update(postRef, "likesCount", FieldValue.increment(-1))
                    false
                } else {
                    // LIKE
                    tx.set(
                        likeRef,
                        mapOf(
                            "userId" to userId,
                            "likedAt" to com.google.firebase.Timestamp.now()
                        )
                    )
                    tx.update(postRef, "likesCount", FieldValue.increment(1))
                    true
                }
            }.await()

            Log.d("like_error", "Transaction complete. New liked state = $result for post=$postId user=$userId")
            Resource.Success(result)

        } catch (e: Exception) {
            Log.e("like_error", "togglePostLike failed: ${e.message}", e)
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
                .document(userId)  // ✅ Firebase UID
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
                Log.d("LikeRepo", "Fetched likes count for $postId: $likesCount")
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

                Log.d("LikeRepo", "Fetched ${userIds.size} likes for post: $postId")
                trySend(userIds)
            }

        awaitClose { listener.remove() }
    }
}