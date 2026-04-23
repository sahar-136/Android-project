package com.example.version.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Post(
    @DocumentId
    val id: String = "",  // ✅ lowercase 'i'
    val userId: String = "",
    val userName: String = "",
    val userProfileUrl: String = "",
    val photoUrl: String = "",
    val caption: String = "",
    val uploadTimestamp: Timestamp = Timestamp.now(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isTrending: Boolean = false,
    val deleted: Boolean = false,
    val location: String = "",
    val tags: List<String> = emptyList()
) {
    constructor() : this(
        id = "",  // ✅ lowercase 'i'
        userId = "",
        userName = "",
        userProfileUrl = "",
        photoUrl = "",
        caption = "",
        uploadTimestamp = Timestamp.now(),
        likesCount = 0,
        commentsCount = 0,
        isTrending = false,
        deleted = false,
        location = "",
        tags = emptyList()
    )
}