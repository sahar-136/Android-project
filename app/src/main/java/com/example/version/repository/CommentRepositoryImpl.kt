package com.example.version.repository

import android.util.Log
import com.example.version.models.Comment
import com.example.version.models.Notification
import com.example.version.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository  // ✅ NEW
) : CommentRepository {

    override suspend fun addComment(
        postId: String,
        userId: String,
        userName: String,
        commentText: String
    ): Resource<Boolean> {
        return try {
            val postRef = firestore.collection("posts").document(postId)
            val commentRef = postRef.collection("comments").document()

            val comment = Comment(
                postId = postId,
                userId = userId,
                userName = userName,
                commentText = commentText,
                commentDatetime = com.google.firebase.Timestamp.now(),
                likeCount = 0
            )

            commentRef.set(comment).await()
            postRef.update("commentsCount", FieldValue.increment(1)).await()

            // ✅ CREATE NOTIFICATION + SEND PUSH
            val postDoc = firestore.collection("posts").document(postId).get().await()
            val postOwnerId = postDoc.getString("userId") ?: ""
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserName = currentUser?.displayName ?: userName
            val currentUserImage = currentUser?.photoUrl?.toString() ?: ""

            if (postOwnerId.isNotBlank() && postOwnerId != userId) {
                // ✅ SAVE IN FIRESTORE
                val notification = Notification(
                    notificationId = "",
                    recipientUserId = postOwnerId,
                    senderUserId = userId,
                    senderName = currentUserName,
                    senderProfileImage = currentUserImage,
                    postId = postId,
                    type = "comment",
                    message = "$currentUserName commented on your photo",
                    timestamp = com.google.firebase.Timestamp.now(),
                    isRead = false
                )

                notificationRepository.createNotification(notification)

                // ✅ SEND PUSH NOTIFICATION
                sendPushNotification(
                    recipientUserId = postOwnerId,
                    senderName = currentUserName,
                    message = "$currentUserName commented on your photo",
                    postId = postId,
                    type = "comment"
                )

                Log.d("CommentRepo", "✅ In-app + Push notification created for comment on post: $postId")
            }

            Log.d("CommentRepo", "✅ Comment added successfully")
            Resource.Success(true)
        } catch (e: Exception) {
            Log.e("CommentRepo", "❌ Failed to add comment: ${e.message}")
            Resource.Error(e.message ?: "Failed to add comment")
        }
    }

    override suspend fun getCommentsByPostId(postId: String): Flow<Resource<List<Comment>>> = callbackFlow {
        trySend(Resource.Loading)

        val query = firestore
            .collection("posts")
            .document(postId)
            .collection("comments")

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed to fetch comments"))
                return@addSnapshotListener
            }

            val comments = snapshot?.toObjects(Comment::class.java).orEmpty()
            val sorted = comments.sortedByDescending { it.commentDatetime?.toDate()?.time ?: 0L }
            trySend(Resource.Success(sorted))
        }

        awaitClose { listener.remove() }
    }

    override suspend fun deleteComment(postId: String, commentId: String): Resource<Boolean> {
        return try {
            val postRef = firestore.collection("posts").document(postId)
            postRef.collection("comments").document(commentId).delete().await()
            postRef.update("commentsCount", FieldValue.increment(-1)).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete comment")
        }
    }

    override suspend fun likeComment(postId: String, commentId: String): Resource<Boolean> {
        return try {
            firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId)
                .update("likeCount", FieldValue.increment(1))
                .await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to like comment")
        }
    }

    override suspend fun unlikeComment(postId: String, commentId: String): Resource<Boolean> {
        return try {
            firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId)
                .update("likeCount", FieldValue.increment(-1))
                .await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unlike comment")
        }
    }

    // ✅ SEND PUSH NOTIFICATION FUNCTION
    private suspend fun sendPushNotification(
        recipientUserId: String,
        senderName: String,
        message: String,
        postId: String,
        type: String
    ) {
        try {
            // ✅ GET FCM TOKEN FROM USER
            val userDoc = firestore.collection("users").document(recipientUserId).get().await()
            val fcmToken = userDoc.getString("fcmToken")

            if (!fcmToken.isNullOrBlank()) {
                Log.d("CommentRepo", "✅ Push notification prepared - Type: $type")
                Log.d("CommentRepo", "   FCM Token: ${fcmToken.take(20)}...")
                Log.d("CommentRepo", "   Message: $message")
                Log.d("CommentRepo", "   PostId: $postId")

                // TODO: Call backend API to send push notification
                // Backend ko FCM token + message + postId send karengy
            } else {
                Log.d("CommentRepo", "⚠️ No FCM token found for user: $recipientUserId")
            }
        } catch (e: Exception) {
            Log.e("CommentRepo", "❌ Error preparing push notification: ${e.message}")
        }
    }
}