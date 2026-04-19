package com.example.version.repository

import android.net.Uri
import com.example.version.models.User
import com.example.version.models.Post
import com.example.version.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    override suspend fun getCurrentUser(): Resource<User> {
        return try {
            val userId = auth.currentUser?.uid ?: return Resource.Error("User not authenticated")
            val doc = firestore.collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java) ?: return Resource.Error("User profile not found")
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load user")
        }
    }

    override suspend fun updateProfile(
        userId: String,
        name: String,
        username: String,
        email: String,
        bio: String
    ): Resource<Boolean> {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val currentUser = userDoc.toObject(User::class.java)
            if (currentUser == null) return Resource.Error("User not found")

            if (username != currentUser.username) {
                val isUsernameAvailable = isUsernameAvailable(username, userId)
                if (!isUsernameAvailable)
                    return Resource.Error("Username is already taken.")
            }

            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "username" to username,
                "email" to email,
                "bio" to bio
            )
            firestore.collection("users").document(userId).update(updates).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }

    override suspend fun updateProfileImage(userId: String, imageUri: Uri): Resource<String> {
        return try {
            val fileName = "profile_${userId}_${System.currentTimeMillis()}.jpg"
            val ref = storage.reference.child("profileImages/$userId/$fileName")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            firestore.collection("users").document(userId)
                .update("profileImageUrl", downloadUrl)
                .await()
            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile image")
        }
    }

    override suspend fun isUsernameAvailable(username: String, currentUserId: String): Boolean {
        val result = firestore.collection("users")
            .whereEqualTo("username", username)
            .get().await()
        return result.isEmpty || result.documents.all { it.id == currentUserId }
    }

    // ✅ NEW: get all posts by userId
    override suspend fun getPostsByUser(userId: String): List<Post> {
        return try {
            firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Post::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getUserById(userId: String): Resource<User> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) Resource.Success(user) else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error fetching user")
        }
    }
}