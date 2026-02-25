package com.example.version.repository
import com.example.version.models.User
import com.example.version.util.Resource

interface AuthRepository {
    suspend fun registerWithEmail(name: String, email: String, password: String): Resource<User>
    suspend fun loginWithEmail(email: String, password: String): Resource<User>
    fun logout()
    fun getCurrentUserId(): String?
}