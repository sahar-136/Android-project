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

    // ============================================
    // EXISTING: Feed posts (Real-time)
    // ============================================
    private val _feedPosts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading)
    val feedPosts: StateFlow<Resource<List<Post>>> = _feedPosts.asStateFlow()

    // ============================================
    // EXISTING: Comment counts map (post ID -> count)
    // ============================================
    private val _commentCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val commentCounts: StateFlow<Map<String, Int>> = _commentCounts.asStateFlow()

    // ============================================
    // NEW: Like status map (post ID -> is liked)
    // ============================================
    private val _likeStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val likeStatus: StateFlow<Map<String, Boolean>> = _likeStatus.asStateFlow()

    // ============================================
    // NEW: Like counts map (post ID -> count)
    // ============================================
    private val _likeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val likeCounts: StateFlow<Map<String, Int>> = _likeCounts.asStateFlow()

    // ============================================
    // INIT: Load feed posts
    // ============================================
    init {
        loadFeed()
    }

    // ============================================
    // LOAD FEED POSTS (Real-time)
    // ============================================
    private fun loadFeed() {
        viewModelScope.launch {
            feedRepository.getFeedPosts()
                .collect { result ->
                    _feedPosts.value = result

                    // When posts load, fetch data for each post
                    if (result is Resource.Success) {
                        result.data.forEach { post ->
                            fetchPostCommentsCount(post.postId)
                            fetchPostLikeStatus(post.postId)
                            fetchPostLikesCount(post.postId)
                        }
                    }
                }
        }
    }

    // ============================================
    // Fetch comments count for single post
    // ============================================
    private fun fetchPostCommentsCount(postId: String) {
        viewModelScope.launch {
            feedRepository.getPostCommentsCount(postId)
                .collect { count ->
                    Log.d("count", "$count")
                    _commentCounts.update { currentMap ->
                        currentMap + (postId to count)
                    }
                }
        }
    }

    // ============================================
    // NEW: Fetch like status for single post
    // ============================================
    private fun fetchPostLikeStatus(postId: String) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            val isLiked = likeRepository.isPostLikedByUser(postId, userId)
            _likeStatus.update { currentMap ->
                currentMap + (postId to isLiked)
            }
        }
    }

    // ============================================
    // NEW: Fetch likes count for single post (Real-time)
    // ============================================
    private fun fetchPostLikesCount(postId: String) {
        viewModelScope.launch {
            likeRepository.getPostLikesCount(postId)
                .collect { count ->
                    _likeCounts.update { currentMap ->
                        currentMap + (postId to count)
                    }
                }
        }
    }

    // ============================================
    // NEW: Toggle like for a post
    // ============================================
    fun togglePostLike(postId: String) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            val result = likeRepository.togglePostLike(postId, userId)

            if (result is Resource.Success) {
                val newStatus = result.data
                _likeStatus.update { currentMap ->
                    currentMap + (postId to newStatus)
                }
                fetchPostLikesCount(postId)
                Log.d("FeedVM", "Post like toggled: $postId - $newStatus")
            }
        }
    }

    // ============================================
    // Get comment count for specific post
    // ============================================
    fun getCommentCount(postId: String): Int {
        return _commentCounts.value[postId] ?: 0
    }

    // ============================================
    // NEW: Get like count for specific post
    // ============================================
    fun getLikeCount(postId: String): Int {
        return _likeCounts.value[postId] ?: 0
    }

    // ============================================
    // NEW: Get like status for specific post
    // ============================================
    fun isPostLiked(postId: String): Boolean {
        return _likeStatus.value[postId] ?: false
    }
}