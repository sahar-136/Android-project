package com.example.version.repository

import android.util.Log
import com.example.version.models.Comment
import com.example.version.util.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommentRepository {

    override suspend fun addComment(
        postId: String,
        userId: String,
        userName: String,
        commentText: String
    ): Resource<Boolean> {
        return try {
            val postRef = firestore.collection("posts").document(postId)
            val commentRef = postRef.collection("comments").document()

            val comment = Comment(
                postId = postId,
                userId = userId,
                userName = userName,
                commentText = commentText,
                commentDatetime = com.google.firebase.Timestamp.now(),
                likeCount = 0
            )

            commentRef.set(comment).await()

            // ✅ YE LINE UPDATED - اب FieldValue.increment استعمال ہو رہا ہے
            postRef.update("commentsCount", FieldValue.increment(1)).await()

            Log.d("CommentRepo", "✅ Comment added successfully for post: $postId")
            Log.d("CommentRepo", "✅ Comment count incremented for post: $postId")

            Resource.Success(true)
        } catch (e: Exception) {
            Log.e("CommentRepo", "❌ Failed to add comment: ${e.message}")
            Resource.Error(e.message ?: "Failed to add comment")
        }
    }

    override suspend fun getCommentsByPostId(postId: String): Flow<Resource<List<Comment>>> = callbackFlow {
        trySend(Resource.Loading)

        val query = firestore
            .collection("posts")
            .document(postId)
            .collection("comments")

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("CommentRepo", "❌ Error fetching comments: ${error.message}")
                trySend(Resource.Error(error.message ?: "Failed to fetch comments"))
                return@addSnapshotListener
            }

            val comments = snapshot?.toObjects(Comment::class.java).orEmpty()
            val sorted = comments.sortedByDescending { it.commentDatetime?.toDate()?.time ?: 0L }

            Log.d("CommentRepo", "✅ Loaded ${sorted.size} comments for post: $postId")
            trySend(Resource.Success(sorted))
        }

        awaitClose { listener.remove() }
    }

    override suspend fun deleteComment(postId: String, commentId: String): Resource<Boolean> {
        return try {
            val postRef = firestore.collection("posts").document(postId)

            // ✅ Comment delete کریں
            postRef.collection("comments").document(commentId).delete().await()

            // ✅ Comment count کو decrement کریں
            postRef.update("commentsCount", FieldValue.increment(-1)).await()

            Log.d("CommentRepo", "✅ Comment deleted successfully for post: $postId")
            Log.d("CommentRepo", "✅ Comment count decremented for post: $postId")

            Resource.Success(true)
        } catch (e: Exception) {
            Log.e("CommentRepo", "❌ Failed to delete comment: ${e.message}")
            Resource.Error(e.message ?: "Failed to delete comment")
        }
    }
}