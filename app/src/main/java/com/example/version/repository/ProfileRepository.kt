package com.example.version.repository

import android.net.Uri
import com.example.version.models.User
import com.example.version.models.Post
import com.example.version.util.Resource

interface ProfileRepository {
    suspend fun getCurrentUser(): Resource<User>
    suspend fun updateProfile(
        userId: String,
        name: String,
        username: String,
        email: String,
        bio: String
    ): Resource<Boolean>
    suspend fun updateProfileImage(
        userId: String,
        imageUri: Uri
    ): Resource<String>
    suspend fun isUsernameAvailable(username: String, currentUserId: String): Boolean

    // ➡️ Add this to get all posts for a user
    suspend fun getPostsByUser(userId: String): List<Post>

    suspend fun getUserById(userId: String): Resource<User>
}