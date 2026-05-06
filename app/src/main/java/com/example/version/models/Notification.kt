package com.example.version.models
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Notification(
    @DocumentId
    val notificationId: String = "",
    val recipientUserId: String = "",  // Jo user ko notification milta hai
    val senderUserId: String = "",     // Jo action karta hai (like/comment)
    val senderName: String = "",
    val senderProfileImage: String = "",
    val postId: String = "",           // Kis post ke liye
    val type: String = "",             // "like" ya "comment"
    val message: String = "",          // "Azmat liked your photo"
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val actionUrl: String = ""         // Post ID for navigation
) {
    constructor() : this(
        notificationId = "",
        recipientUserId = "",
        senderUserId = "",
        senderName = "",
        senderProfileImage = "",
        postId = "",
        type = "",
        message = "",
        timestamp = Timestamp.now(),
        isRead = false,
        actionUrl = ""
    )
}