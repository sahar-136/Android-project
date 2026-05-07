package com.example.version.repository

import com.example.version.models.Post
import com.example.version.util.Resource
import kotlinx.coroutines.flow.Flow

interface FeedRepository {

    fun getFeedPosts(): Flow<Resource<List<Post>>>

    fun getTrendingPosts(): Flow<Resource<List<Post>>>

    suspend fun getPostCommentsCount(postId: String): Flow<Int>

    suspend fun getPostLikesCount(postId: String): Flow<Int>
}