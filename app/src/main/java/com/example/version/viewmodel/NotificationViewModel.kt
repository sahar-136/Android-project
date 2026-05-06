package com.example.version.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.Notification
import com.example.version.repository.NotificationRepository
import com.example.version.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<Resource<List<Notification>>>(Resource.Loading)
    val notifications: StateFlow<Resource<List<Notification>>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _markAsReadState = MutableStateFlow<Resource<Boolean>?>(null)
    val markAsReadState: StateFlow<Resource<Boolean>?> = _markAsReadState.asStateFlow()

    private val _deleteNotificationState = MutableStateFlow<Resource<Boolean>?>(null)
    val deleteNotificationState: StateFlow<Resource<Boolean>?> = _deleteNotificationState.asStateFlow()

    private val _createNotificationState = MutableStateFlow<Resource<Boolean>?>(null)
    val createNotificationState: StateFlow<Resource<Boolean>?> = _createNotificationState.asStateFlow()

    // Load notifications for current user (Real-time)
    fun loadNotifications(userId: String) {
        Log.d("NotificationVM", "Loading notifications for user: $userId")

        viewModelScope.launch {
            try {
                notificationRepository.getNotifications(userId).collectLatest { result ->
                    Log.d("NotificationVM", "Notifications result: $result")
                    _notifications.value = result
                }
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error loading notifications: ${e.message}")
                _notifications.value = Resource.Error(e.message ?: "Failed to load notifications")
            }
        }
    }

    // Watch unread count in real-time
    fun watchUnreadCount(userId: String) {
        Log.d("NotificationVM", "Watching unread count for user: $userId")

        viewModelScope.launch {
            try {
                notificationRepository.getUnreadCount(userId).collectLatest { count ->
                    Log.d("NotificationVM", "Unread count: $count")
                    _unreadCount.value = count
                }
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error watching unread count: ${e.message}")
            }
        }
    }

    // Mark notification as read
    fun markAsRead(notificationId: String) {
        Log.d("NotificationVM", "Marking notification as read: $notificationId")

        viewModelScope.launch {
            _markAsReadState.value = Resource.Loading
            try {
                val result = notificationRepository.markAsRead(notificationId)
                _markAsReadState.value = result
                Log.d("NotificationVM", "Mark as read result: $result")
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error marking as read: ${e.message}")
                _markAsReadState.value = Resource.Error(e.message ?: "Failed to mark as read")
            }
        }
    }

    // Delete notification
    fun deleteNotification(notificationId: String) {
        Log.d("NotificationVM", "Deleting notification: $notificationId")

        viewModelScope.launch {
            _deleteNotificationState.value = Resource.Loading
            try {
                val result = notificationRepository.deleteNotification(notificationId)
                _deleteNotificationState.value = result
                Log.d("NotificationVM", "Delete result: $result")
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error deleting notification: ${e.message}")
                _deleteNotificationState.value = Resource.Error(e.message ?: "Failed to delete")
            }
        }
    }

    // Create notification (called when user likes/comments)
    fun createNotification(notification: Notification) {
        Log.d("NotificationVM", "Creating notification for user: ${notification.recipientUserId}")

        viewModelScope.launch {
            _createNotificationState.value = Resource.Loading
            try {
                val result = notificationRepository.createNotification(notification)
                _createNotificationState.value = result
                Log.d("NotificationVM", "Create notification result: $result")
            } catch (e: Exception) {
                Log.e("NotificationVM", "Error creating notification: ${e.message}")
                _createNotificationState.value = Resource.Error(e.message ?: "Failed to create")
            }
        }
    }

    // Reset states
    fun resetMarkAsReadState() {
        _markAsReadState.value = null
    }

    fun resetDeleteNotificationState() {
        _deleteNotificationState.value = null
    }

    fun resetCreateNotificationState() {
        _createNotificationState.value = null
    }
}