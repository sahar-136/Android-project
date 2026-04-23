package com.example.version.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.Post
import com.example.version.repository.AuthRepository
import com.example.version.repository.FeedRepository
import com.example.version.repository.LikeRepository
import com.example.version.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val likeRepository: LikeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Feed posts
    private val _feedPosts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading)
    val feedPosts: StateFlow<Resource<List<Post>>> = _feedPosts.asStateFlow()

    // Trending posts
    private val _trendingPosts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading)
    val trendingPosts: StateFlow<Resource<List<Post>>> = _trendingPosts.asStateFlow()
    private var trendingStarted = false

    // Like status map (id -> isLiked)  // ✅ postId → id
    private val _likeStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val likeStatus: StateFlow<Map<String, Boolean>> = _likeStatus.asStateFlow()

    // Like counts map (id -> count)  // ✅ postId → id
    private val _likeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val likeCounts: StateFlow<Map<String, Int>> = _likeCounts.asStateFlow()

    init {
        loadFeed()
    }

    // ✅ Method to refresh feed
    fun refreshFeed() {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            feedRepository.getFeedPosts().collect { result ->
                _feedPosts.value = result

                if (result is Resource.Success) {
                    result.data.forEach { post ->
                        fetchPostLikeStatus(post.id)  // ✅ postId → id
                        fetchPostLikesCount(post.id)  // ✅ postId → id
                    }
                }
            }
        }
    }

    // Call when Trending tab opens first time
    fun loadTrendingOnce() {
        if (trendingStarted) return
        trendingStarted = true

        viewModelScope.launch {
            feedRepository.getTrendingPosts().collect { result ->
                _trendingPosts.value = result

                if (result is Resource.Success) {
                    result.data.forEach { post ->
                        fetchPostLikeStatus(post.id)  // ✅ postId → id
                        fetchPostLikesCount(post.id)  // ✅ postId → id
                    }
                }
            }
        }
    }

    private fun fetchPostLikeStatus(postId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val isLiked = likeRepository.isPostLikedByUser(postId, userId)
            _likeStatus.update { it + (postId to isLiked) }
        }
    }

    private fun fetchPostLikesCount(postId: String) {
        viewModelScope.launch {
            likeRepository.getPostLikesCount(postId).collect { count ->
                _likeCounts.update { it + (postId to count) }
            }
        }
    }

    fun togglePostLike(postId: String) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            val result = likeRepository.togglePostLike(postId, userId)
            if (result is Resource.Success) {
                val newStatus = result.data
                _likeStatus.update { it + (postId to newStatus) }
                fetchPostLikesCount(postId)
                Log.d("FeedVM", "Post like toggled: $postId - $newStatus")
            }
        }
    }

    fun getLikeCount(postId: String): Int = _likeCounts.value[postId] ?: 0
    fun isPostLiked(postId: String): Boolean = _likeStatus.value[postId] ?: false
}