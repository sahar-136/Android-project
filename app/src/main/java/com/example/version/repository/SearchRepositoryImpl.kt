package com.example.version.repository

import com.example.version.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SearchRepository {

    override suspend fun searchUsers(query: String, limit: Long): List<User> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()

        val snapshot = firestore.collection("users")
            .limit(500)
            .get()
            .await()

        val lower = q.lowercase()

        return snapshot.documents.mapNotNull { doc ->
            val user = doc.toObject(User::class.java) ?: return@mapNotNull null

            val usernameMatch = user.username.lowercase().contains(lower)
            val nameMatch = user.name.lowercase().contains(lower)

            if (usernameMatch || nameMatch) user else null
        }
    }
}