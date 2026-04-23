package com.example.version.repository

import com.example.version.models.User

interface SearchRepository {
    suspend fun searchUsers(query: String, limit: Long = 30): List<User>
}