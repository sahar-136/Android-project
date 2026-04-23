package com.example.version.repository

import android.net.Uri
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
        try {
            // Get user profile
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: return Resource.Error("User not found")

            // Check upload limit
            if (!user.canUploadToday()) {
                return Resource.Error("You can only upload one photo per day!")
            }

            // Upload photo to storage
            val fileName = "${userId}_${System.currentTimeMillis()}.jpg"
            val photoRef = storage.reference.child("postImages/$userId/$fileName")
            photoRef.putFile(fileUri).await()
            val downloadUrl = photoRef.downloadUrl.await().toString()

            // Create post document
            val postDoc = firestore.collection("posts").document()

            // ✅ CORRECT: Don't include postId - @DocumentId will auto-populate
            val postMap = mapOf(
                "userId" to userId,
                "userName" to user.name,
                "userProfileUrl" to user.profileImageUrl,
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
            postDoc.set(postMap).await()

            // Update user stats
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "lastUploadDate" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "totalPhotos" to (user.totalPhotos + 1)
                    )
                ).await()

            // ✅ CORRECT: Return Post with id (not postId)
            val post = Post(
                id = postDoc.id,  // ← id, not postId
                userId = userId,
                userName = user.name,
                userProfileUrl = user.profileImageUrl,
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

            return Resource.Success(post)
        } catch (e: Exception) {
            return Resource.Error(e.message ?: "Failed to upload photo")
        }
    }
}