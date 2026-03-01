package com.example.version.repository

import com.example.version.models.User
import com.example.version.util.Resource

interface AuthRepository {
    suspend fun registerWithEmail(
        name: String,
        email: String,
        username: String,   // <-- Username bhi required
        password: String
    ): Resource<User>

    suspend fun loginWithEmail(
        email: String,
        password: String
    ): Resource<User>

    // idToken: from Google OneTap/SignIn API
    // username: Only needed IF Google user is new (handle in ViewModel/UI)
    //    -- see note below for detail
    suspend fun loginWithGoogle(
        idToken: String,
        username: String? = null  // <-- Pass if user is new
    ): Resource<User>

    // Username availability check (for instant feedback)
    suspend fun isUsernameAvailable(username: String): Boolean

    fun logout()
    fun getCurrentUserId(): String?
}