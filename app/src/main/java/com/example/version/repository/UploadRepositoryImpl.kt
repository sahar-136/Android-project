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
            // 1. Get current user's profile (for name, profilePhoto, and last upload)
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: return Resource.Error("User not found")

            // 2. Check if user already uploaded today
            if (!user.canUploadToday()) {
                return Resource.Error("You can only upload one photo per day!")
            }

            // 3. Upload file to Firebase Storage
            val fileName = "${userId}_${System.currentTimeMillis()}.jpg"
            val photoRef = storage.reference.child("postImages/$userId/$fileName")
            val uploadTask = photoRef.putFile(fileUri).await()

            // 4. Get photo URL
            val downloadUrl = photoRef.downloadUrl.await().toString()

            // 5. Create Post object
            val timeNow = Timestamp.now()
            val post = Post(
                userId = userId,
                userName = user.name,
                userProfileUrl = user.profileImageUrl,
                photoUrl = downloadUrl,
                caption = caption ?: "",
                uploadTimestamp = timeNow
            )

            // 6. Write to Firestore ("posts" collection)
            val postDoc = firestore.collection("posts").document()
            val finalPost = post.copy(postId = postDoc.id)
            postDoc.set(finalPost).await()

            // 7. Update user's lastUploadDate & totalPhotos
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "lastUploadDate" to timeNow,
                        "totalPhotos" to (user.totalPhotos + 1)
                    )
                ).await()

            return Resource.Success(finalPost)
        } catch (e: Exception) {
            return Resource.Error(e.message ?: "Failed to upload photo")
        }
    }
}