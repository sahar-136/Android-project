package com.example.version.repository

import com.example.version.models.Post
import com.example.version.util.Resource
import kotlinx.coroutines.flow.Flow

interface FeedRepository {

    // GET ALL FEED POSTS (Real-time)
    fun getFeedPosts(): Flow<Resource<List<Post>>>

    // GET TRENDING POSTS (Real-time)
    fun getTrendingPosts(): Flow<Resource<List<Post>>>

    // GET POST COMMENTS COUNT (Real-time)
    suspend fun getPostCommentsCount(postId: String): Flow<Int>
}