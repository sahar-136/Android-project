package com.example.version.models

import com.google.firebase.Timestamp

data class Like(
    val userId: String = "",
    val likedAt: Timestamp = Timestamp.now()
)