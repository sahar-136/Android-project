package com.example.version.repository
import com.example.version.models.Notification
import com.example.version.util.Resource
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

    // Get all notifications for current user (Real-time)
    fun getNotifications(userId: String): Flow<Resource<List<Notification>>>

    // Get unread notification count (Real-time)
    fun getUnreadCount(userId: String): Flow<Int>

    // Mark notification as read
    suspend fun markAsRead(notificationId: String): Resource<Boolean>

    // Delete notification
    suspend fun deleteNotification(notificationId: String): Resource<Boolean>

    // Create notification (when user likes/comments)
    suspend fun createNotification(notification: Notification): Resource<Boolean>
}