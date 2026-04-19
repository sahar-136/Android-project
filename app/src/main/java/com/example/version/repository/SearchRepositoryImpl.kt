package com.example.version.repository

import com.example.version.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SearchRepository {
    override suspend fun searchUsers(query: String): List<User> {
        // Basic example: search by name or username (adjust as needed!)
        val result = firestore.collection("users")
            .get()
            .await()
        return result.documents.mapNotNull { doc ->
            val user = doc.toObject(User::class.java)
            if (
                user != null &&
                (user.name.contains(query, ignoreCase = true) || user.username.contains(query, ignoreCase = true))
            ) user else null
        }
    }
}