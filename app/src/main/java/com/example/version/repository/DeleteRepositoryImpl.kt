package com.example.version.repository
import android.util.Log
import com.example.version.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DeletePostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : DeletePostRepository {

    override suspend fun deletePost(
        postId: String,
        userId: String,
        photoUrl: String
    ): Resource<Boolean> {
        return try {
            Log.d("DeletePostRepo", "Deleting post: $postId for user: $userId")

            // ✅ Step 1: Delete image from Cloud Storage
            Log.d("DeletePostRepo", "Deleting photo from storage...")
            try {
                val photoRef = storage.getReferenceFromUrl(photoUrl)
                photoRef.delete().await()
                Log.d("DeletePostRepo", "Photo deleted from storage")
            } catch (e: Exception) {
                Log.e("DeletePostRepo", "Error deleting photo: ${e.message}")
                // Continue anyway
            }

            // ✅ Step 2: Delete post document
            Log.d("DeletePostRepo", "Deleting post document...")
            firestore.collection("posts").document(postId).delete().await()
            Log.d("DeletePostRepo", "Post document deleted")

            // ✅ Step 3: Delete all likes for this post
            Log.d("DeletePostRepo", "Deleting likes...")
            val likes = firestore.collection("likes")
                .whereEqualTo("postId", postId)
                .get()
                .await()

            for (like in likes.documents) {
                like.reference.delete().await()
            }
            Log.d("DeletePostRepo", "Likes deleted: ${likes.size()}")

            // ✅ Step 4: Delete all comments for this post
            Log.d("DeletePostRepo", "Deleting comments...")
            val comments = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .get()
                .await()

            for (comment in comments.documents) {
                comment.reference.delete().await()
            }
            Log.d("DeletePostRepo", "Comments deleted: ${comments.size()}")

            // ✅ Step 5: Update user totalPhotos count
            Log.d("DeletePostRepo", "Updating user totalPhotos...")
            firestore.collection("users").document(userId).update(
                "totalPhotos", com.google.firebase.firestore.FieldValue.increment(-1)
            ).await()
            Log.d("DeletePostRepo", "User totalPhotos updated")

            Log.d("DeletePostRepo", "Post deleted successfully")
            Resource.Success(true)
        } catch (e: Exception) {
            Log.e("DeletePostRepo", "Error deleting post: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to delete post")
        }
    }
}