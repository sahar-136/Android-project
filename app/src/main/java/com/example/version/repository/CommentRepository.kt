package com.example.version.repository
import com.example.version.models.Comment
import com.example.version.util.Resource
import kotlinx.coroutines.flow.Flow

interface CommentRepository {

    // ============================================
    // ADD COMMENT TO POST
    // ============================================
    suspend fun addComment(
        postId: String,
        userId: String,
        userName: String,
        commentText: String
    ): Resource<Boolean>

    // ============================================
    // GET ALL COMMENTS BY POST ID (Real-time)
    // ============================================
    suspend fun getCommentsByPostId(postId: String): Flow<Resource<List<Comment>>>

    // ============================================
    // DELETE COMMENT
    // ============================================
    suspend fun deleteComment(
        postId: String,
        commentId: String
    ): Resource<Boolean>

    // ============================================
    // LIKE COMMENT (Increment likeCount)
    // ============================================
    suspend fun likeComment(
        postId: String,
        commentId: String
    ): Resource<Boolean>

    // ============================================
    // UNLIKE COMMENT (Decrement likeCount)
    // ============================================
    suspend fun unlikeComment(
        postId: String,
        commentId: String
    ): Resource<Boolean>
}