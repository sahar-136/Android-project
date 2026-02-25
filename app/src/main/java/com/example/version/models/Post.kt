package com.example.version.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Post(
    @DocumentId
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileUrl: String = "",
    val photoUrl: String = "",
    val caption: String = "",
    val uploadTimestamp: Timestamp = Timestamp.now(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isTrending: Boolean = false,
    val isDeleted: Boolean = false,
    val location: String = "", // optional
    val tags: List<String> = emptyList() // optional for filtering/trending
) {
    // Empty constructor for Firestore
    constructor() : this(
        postId = "",
        userId = "",
        userName = "",
        userProfileUrl = "",
        photoUrl = "",
        caption = "",
        uploadTimestamp = Timestamp.now(),
        likesCount = 0,
        commentsCount = 0,
        isTrending = false,
        isDeleted = false,
        location = "",
        tags = emptyList()
    )

    // Helper: was post uploaded today?
    fun isUploadedToday(): Boolean {
        val today = System.currentTimeMillis()
        val upload = uploadTimestamp.toDate().time
        val oneDayInMillis = 24 * 60 * 60 * 1000
        return (today - upload) < oneDayInMillis
    }
}