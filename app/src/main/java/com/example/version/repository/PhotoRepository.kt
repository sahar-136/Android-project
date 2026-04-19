package com.example.version.repository
import com.example.version.models.Post
import com.example.version.util.Resource
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    // ============================================
    // GET SINGLE POST BY ID (Real-time)
    // ============================================
    suspend fun getPostById(postId: String): Flow<Resource<Post>>

    // ============================================
    // GET COMMENTS FOR A POST (Real-time)
    // ============================================
    suspend fun getCommentsByPostId(postId: String): Flow<Resource<List<Map<String, Any>>>>

    // ============================================
    // GET LIKES COUNT FOR POST (Real-time)
    // ============================================
    suspend fun getPostLikesCount(postId: String): Flow<Int>

    // ============================================
    // CHECK IF CURRENT USER LIKED POST
    // ============================================
    suspend fun isPostLikedByUser(
        postId: String,
        userId: String
    ): Boolean

    // ============================================
    // TOGGLE LIKE ON POST
    // ============================================
    suspend fun toggleLike(
        postId: String,
        userId: String
    ): Resource<Boolean>

    // ============================================
    // GET USER INFO (For post creator)
    // ============================================
    suspend fun getUserById(userId: String): Flow<Resource<com.example.version.models.User>>
}