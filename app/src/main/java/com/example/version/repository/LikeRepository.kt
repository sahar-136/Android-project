package com.example.version.repository

import com.example.version.util.Resource
import kotlinx.coroutines.flow.Flow

interface LikeRepository {

    // ============================================
    // TOGGLE LIKE ON POST (Like/Unlike)
    // ============================================
    suspend fun togglePostLike(
        postId: String,
        userId: String
    ): Resource<Boolean>

    // ============================================
    // CHECK IF POST IS LIKED BY USER
    // ============================================
    suspend fun isPostLikedByUser(
        postId: String,
        userId: String
    ): Boolean

    // ============================================
    // GET POST LIKES COUNT (Real-time)
    // ============================================
    suspend fun getPostLikesCount(postId: String): Flow<Int>

    // ============================================
    // GET ALL USERS WHO LIKED POST
    // ============================================
    suspend fun getPostLikes(postId: String): Flow<List<String>>

    // ============================================
    // TOGGLE LIKE ON COMMENT (Like/Unlike)
    // ============================================
    suspend fun toggleCommentLike(
        postId: String,
        commentId: String,
        userId: String
    ): Resource<Boolean>

    // ============================================
    // CHECK IF COMMENT IS LIKED BY USER
    // ============================================
    suspend fun isCommentLikedByUser(
        postId: String,
        commentId: String,
        userId: String
    ): Boolean
}