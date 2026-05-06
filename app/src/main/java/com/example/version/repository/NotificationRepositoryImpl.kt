package com.example.version.repository
import android.util.Log
import com.example.version.models.Notification
import com.example.version.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<Resource<List<Notification>>> = callbackFlow {
        Log.d("NotificationRepo", "Fetching notifications for user: $userId")

        try {
            val listener = firestore.collection("notifications")
                .whereEqualTo("recipientUserId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)  // Latest 20 notifications
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("NotificationRepo", "Error fetching notifications: ${error.message}")
                        trySend(Resource.Error(error.message ?: "Failed to fetch notifications"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val notifications = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Notification::class.java)
                        }
                        Log.d("NotificationRepo", "Notifications fetched: ${notifications.size}")
                        trySend(Resource.Success(notifications))
                    }
                }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Exception: ${e.message}")
            trySend(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> = callbackFlow {
        Log.d("NotificationRepo", "Fetching unread count for user: $userId")

        try {
            val listener = firestore.collection("notifications")
                .whereEqualTo("recipientUserId", userId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("NotificationRepo", "Error fetching unread count: ${error.message}")
                        trySend(0)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val unreadCount = snapshot.size()
                        Log.d("NotificationRepo", "Unread count: $unreadCount")
                        trySend(unreadCount)
                    }
                }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Exception: ${e.message}")
            trySend(0)
        }
    }

    override suspend fun markAsRead(notificationId: String): Resource<Boolean> {
        return try {
            Log.d("NotificationRepo", "Marking notification as read: $notificationId")

            firestore.collection("notifications").document(notificationId)
                .update("isRead", true)
                .await()

            Log.d("NotificationRepo", "Notification marked as read")
            Resource.Success(true)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error marking as read: ${e.message}")
            Resource.Error(e.message ?: "Failed to mark as read")
        }
    }

    override suspend fun deleteNotification(notificationId: String): Resource<Boolean> {
        return try {
            Log.d("NotificationRepo", "Deleting notification: $notificationId")

            firestore.collection("notifications").document(notificationId)
                .delete()
                .await()

            Log.d("NotificationRepo", "Notification deleted")
            Resource.Success(true)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error deleting notification: ${e.message}")
            Resource.Error(e.message ?: "Failed to delete notification")
        }
    }

    override suspend fun createNotification(notification: Notification): Resource<Boolean> {
        return try {
            Log.d("NotificationRepo", "Creating notification for user: ${notification.recipientUserId}")

            firestore.collection("notifications")
                .add(notification)
                .await()

            Log.d("NotificationRepo", "Notification created successfully")
            Resource.Success(true)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error creating notification: ${e.message}")
            Resource.Error(e.message ?: "Failed to create notification")
        }
    }
}