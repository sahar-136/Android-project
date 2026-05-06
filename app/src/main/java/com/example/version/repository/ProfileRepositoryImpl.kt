package com.example.version.repository

import android.net.Uri
import android.util.Log
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
            Log.d("ProfileRepo", "Fetching current user: $userId")

            val doc = firestore.collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java) ?: return Resource.Error("User profile not found")

            Log.d("ProfileRepo", "User fetched: ${user.name}, profileImageUrl: ${user.profileImageUrl}")
            Resource.Success(user)
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error fetching current user: ${e.message}", e)
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
            Log.d("ProfileRepo", "Updating profile for $userId: name=$name, username=$username")

            val userDoc = firestore.collection("users").document(userId).get().await()
            val currentUser = userDoc.toObject(User::class.java)
            if (currentUser == null) {
                Log.e("ProfileRepo", "User not found: $userId")
                return Resource.Error("User not found")
            }

            if (username != currentUser.username) {
                Log.d("ProfileRepo", "Username changed, checking availability...")
                val isUsernameAvailable = isUsernameAvailable(username, userId)
                if (!isUsernameAvailable) {
                    Log.w("ProfileRepo", "Username already taken: $username")
                    return Resource.Error("Username is already taken.")
                }
            }

            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "username" to username,
                "email" to email,
                "bio" to bio
            )

            firestore.collection("users").document(userId).update(updates).await()
            Log.d("ProfileRepo", "Profile updated successfully")
            Resource.Success(true)
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error updating profile: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }

    override suspend fun updateProfileImage(userId: String, imageUri: Uri): Resource<String> {
        return try {
            Log.d("ProfileRepo", "Starting image upload for userId=$userId, uri=$imageUri")

            val fileName = "profile_${userId}_${System.currentTimeMillis()}.jpg"
            val ref = storage.reference.child("profileImages/$userId/$fileName")

            // ✅ Step 1: Upload file
            Log.d("ProfileRepo", "Uploading file to storage: $fileName")
            ref.putFile(imageUri).await()
            Log.d("ProfileRepo", "File uploaded successfully")

            // ✅ Step 2: Get download URL
            Log.d("ProfileRepo", "Fetching download URL...")
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d("ProfileRepo", "Download URL: $downloadUrl")

            // ✅ Step 3: Update Firestore SYNCHRONOUSLY with error handling
            Log.d("ProfileRepo", "Updating Firestore with profileImageUrl...")
            try {
                firestore.collection("users").document(userId)
                    .update("profileImageUrl", downloadUrl)
                    .await()  // ✅ Wait for it to complete
                Log.d("ProfileRepo", "Firestore updated successfully with URL: $downloadUrl")
            } catch (firestoreException: Exception) {
                Log.e("ProfileRepo", "Firestore update failed: ${firestoreException.message}")
                throw firestoreException  // ✅ Re-throw to catch block below
            }

            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error during image upload: ${e.message}", e)
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to update profile image")
        }
    }

    override suspend fun isUsernameAvailable(username: String, currentUserId: String): Boolean {
        return try {
            Log.d("ProfileRepo", "Checking username availability: $username")
            val result = firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()

            val available = result.isEmpty || result.documents.all { it.id == currentUserId }
            Log.d("ProfileRepo", "Username available: $available")
            available
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error checking username: ${e.message}", e)
            false
        }
    }

    override suspend fun getPostsByUser(userId: String): List<Post> {
        return try {
            Log.d("ProfileRepo", "Fetching posts for user: $userId")
            val posts = firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Post::class.java) }

            Log.d("ProfileRepo", "Posts fetched: ${posts.size}")
            posts
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error fetching posts: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getUserById(userId: String): Resource<User> {
        return try {
            Log.d("ProfileRepo", "Fetching user by ID: $userId")
            val doc = firestore.collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java)

            if (user != null) {
                Log.d("ProfileRepo", "User found: ${user.name}")
                Resource.Success(user)
            } else {
                Log.w("ProfileRepo", "User not found: $userId")
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error fetching user: ${e.message}", e)
            Resource.Error(e.message ?: "Error fetching user")
        }
    }
}