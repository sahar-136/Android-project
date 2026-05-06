package com.example.version.repository

import android.net.Uri
import android.util.Log
import com.example.version.models.Post
import com.example.version.models.User
import com.example.version.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.collections.emptyList


class UploadRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : UploadRepository {

    override suspend fun uploadPhotoAndCreatePost(
        userId: String,
        fileUri: Uri,
        caption: String?
    ): Resource<Post> {
        return try {
            // Get user profile
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: return Resource.Error("User not found")

            // ✅ LOG: Check if user profileImageUrl exists
            Log.d("UploadRepo", "User found: ${user.name}, profileImageUrl: '${user.profileImageUrl}'")

            // Check upload limit
            if (!user.canUploadToday()) {
                return Resource.Error("You can only upload one photo per day!")
            }

            // Upload photo to storage
            val fileName = "${userId}_${System.currentTimeMillis()}.jpg"
            val photoRef = storage.reference.child("postImages/$userId/$fileName")
            photoRef.putFile(fileUri).await()
            val downloadUrl = photoRef.downloadUrl.await().toString()
            Log.d("UploadRepo", "Photo uploaded: $downloadUrl")

            // Create post document
            val postDoc = firestore.collection("posts").document()

            // ✅ FIX: Ensure userProfileUrl is properly populated
            val userProfileUrl = if (user.profileImageUrl.isNotBlank()) {
                user.profileImageUrl
            } else {
                ""
            }

            val postMap = mapOf(
                "userId" to userId,
                "userName" to user.name,
                "userProfileUrl" to userProfileUrl,  // ✅ Properly handled
                "photoUrl" to downloadUrl,
                "caption" to (caption ?: ""),
                "uploadTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "likesCount" to 0,
                "commentsCount" to 0,
                "isTrending" to false,
                "deleted" to false,
                "location" to "",
                "tags" to emptyList<String>()
            )

            Log.d("UploadRepo", "Creating post with userProfileUrl: '$userProfileUrl'")
            postDoc.set(postMap).await()
            Log.d("UploadRepo", "Post created: ${postDoc.id}")

            // Update user stats
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "lastUploadDate" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "totalPhotos" to (user.totalPhotos + 1)
                    )
                ).await()

            Log.d("UploadRepo", "User stats updated")

            // ✅ CORRECT: Return Post with id (not postId)
            val post = Post(
                id = postDoc.id,
                userId = userId,
                userName = user.name,
                userProfileUrl = userProfileUrl,  // ✅ Use same value
                photoUrl = downloadUrl,
                caption = caption ?: "",
                uploadTimestamp = Timestamp.now(),
                likesCount = 0,
                commentsCount = 0,
                isTrending = false,
                deleted = false,
                location = "",
                tags = emptyList()
            )

            Log.d("UploadRepo", "Returning post: id=${post.id}, userProfileUrl='${post.userProfileUrl}'")
            Resource.Success(post)

        } catch (e: Exception) {
            Log.e("UploadRepo", "Error uploading post: ${e.message}", e)
            return Resource.Error(e.message ?: "Failed to upload photo")
        }
    }
}