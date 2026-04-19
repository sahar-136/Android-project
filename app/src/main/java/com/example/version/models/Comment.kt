package com.example.version.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Comment(
    @DocumentId
    val commentId: String = "",  // ✅ Auto-populated from document ID
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val commentText: String = "",
    val commentDatetime: Timestamp = Timestamp.now(),
    val likeCount: Int = 0
) {
    constructor() : this(
        commentId = "",
        postId = "",
        userId = "",
        userName = "",
        commentText = "",
        commentDatetime = Timestamp.now(),
        likeCount = 0
    )

    fun getTimeAgo(): String {
        val now = System.currentTimeMillis()
        val commentTime = commentDatetime.toDate().time
        val diff = now - commentTime

        return when {
            diff < 60 * 1000 -> "just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            else -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        }
    }
}