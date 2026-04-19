package com.example.version.repository

import com.example.version.models.Comment
import com.example.version.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
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
            postRef.update(
                "commentsCount",
                FieldValue.increment(1)
            ).await()
            Resource.Success(true)
        } catch (e: Exception) {
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
                trySend(Resource.Error(error.message ?: "Failed to fetch comments"))
                return@addSnapshotListener
            }
            val comments = snapshot?.toObjects(Comment::class.java) ?: emptyList()
            val sortedComments = comments.sortedByDescending {
                it.commentDatetime?.toDate()?.time ?: 0
            }
            trySend(Resource.Success(sortedComments))
        }
        // Only the awaitClose block here, last line!
        awaitClose { listener.remove() }
    }

    override suspend fun deleteComment(
        postId: String,
        commentId: String
    ): Resource<Boolean> {
        return try {
            val postRef = firestore.collection("posts").document(postId)
            val commentRef = postRef.collection("comments").document(commentId)
            commentRef.delete().await()
            postRef.update(
                "commentsCount",
                FieldValue.increment(-1)
            ).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete comment")
        }
    }

    override suspend fun likeComment(
        postId: String,
        commentId: String
    ): Resource<Boolean> {
        return try {
            val commentRef = firestore
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId)
            commentRef.update(
                "likeCount",
                FieldValue.increment(1)
            ).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to like comment")
        }
    }

    override suspend fun unlikeComment(
        postId: String,
        commentId: String
    ): Resource<Boolean> {
        return try {
            val commentRef = firestore
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId)
            commentRef.update(
                "likeCount",
                FieldValue.increment(-1)
            ).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unlike comment")
        }
    }
}