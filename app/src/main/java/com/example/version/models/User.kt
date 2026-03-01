package com.example.version.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val username: String = "",               // <-- ADDED FIELD
    val profileImageUrl: String = "",
    val fcmToken: String = "",
    val accountCreateDate: Timestamp = Timestamp.now(),
    val lastUploadDate: Timestamp? = null,
    val totalPhotos: Int = 0,
    val bio: String = "",
    val isVerified: Boolean = false
) {
    // Empty constructor required for Firestore deserialization
    constructor() : this(
        userId = "",
        name = "",
        email = "",
        username = "",                       // <-- ADDED FIELD
        profileImageUrl = "",
        fcmToken = "",
        accountCreateDate = Timestamp.now(),
        lastUploadDate = null,
        totalPhotos = 0,
        bio = "",
        isVerified = false
    )

    // Helper function to check if user can upload today
    fun canUploadToday(): Boolean {
        if (lastUploadDate == null) return true

        val today = System.currentTimeMillis()
        val lastUpload = lastUploadDate.toDate().time
        val oneDayInMillis = 24 * 60 * 60 * 1000

        return (today - lastUpload) >= oneDayInMillis
    }

    // Helper function to get days since last upload
    fun daysSinceLastUpload(): Int {
        if (lastUploadDate == null) return -1

        val today = System.currentTimeMillis()
        val lastUpload = lastUploadDate.toDate().time
        val oneDayInMillis = 24 * 60 * 60 * 1000

        return ((today - lastUpload) / oneDayInMillis).toInt()
    }
}