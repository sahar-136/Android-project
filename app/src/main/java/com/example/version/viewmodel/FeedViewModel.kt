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

    private val _feedPosts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading)
    val feedPosts: StateFlow<Resource<List<Post>>> = _feedPosts.asStateFlow()

    private val _trendingPosts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading)
    val trendingPosts: StateFlow<Resource<List<Post>>> = _trendingPosts.asStateFlow()
    private var trendingStarted = false

    // Like status map (postId → isLiked)
    private val _likeStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val likeStatus: StateFlow<Map<String, Boolean>> = _likeStatus.asStateFlow()

    // Like counts map (postId → count)
    private val _likeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val likeCounts: StateFlow<Map<String, Int>> = _likeCounts.asStateFlow()

    // Comment counts map (postId → count) ✅ YE NAYA FIELD HAI
    private val _commentCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val commentCounts: StateFlow<Map<String, Int>> = _commentCounts.asStateFlow()

    // Track which posts already have listeners active ✅ YE NAYE FIELDS HAIN
    private val postLikeCountListeners = mutableMapOf<String, Boolean>()
    private val postCommentCountListeners = mutableMapOf<String, Boolean>()

    init {
        loadFeed()
    }

    fun refreshFeed() {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            feedRepository.getFeedPosts().collect { result ->
                _feedPosts.value = result

                if (result is Resource.Success) {
                    result.data.forEach { post ->
                        Log.d("FeedVM", "Setting up listeners for post: ${post.id}")
                        fetchPostLikeStatus(post.id)
                        fetchPostLikesCount(post.id)
                        fetchPostCommentCount(post.id)  // ✅ YE NAYA LINE HAI
                    }
                }
            }
        }
    }

    fun loadTrendingOnce() {
        if (trendingStarted) return
        trendingStarted = true

        viewModelScope.launch {
            feedRepository.getTrendingPosts().collect { result ->
                _trendingPosts.value = result

                if (result is Resource.Success) {
                    result.data.forEach { post ->
                        Log.d("FeedVM", "Setting up listeners for trending post: ${post.id}")
                        fetchPostLikeStatus(post.id)
                        fetchPostLikesCount(post.id)
                        fetchPostCommentCount(post.id)  // ✅ YE NAYA LINE HAI
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
            Log.d("FeedVM", "Like status for $postId: $isLiked")
        }
    }

    // ✅ YE FUNCTION UPDATED HAI - ab feedRepository se like count lete hain
    private fun fetchPostLikesCount(postId: String) {
        if (postLikeCountListeners[postId] == true) {
            Log.d("FeedVM", "Like listener already active for $postId, skipping...")
            return
        }

        postLikeCountListeners[postId] = true

        viewModelScope.launch {
            feedRepository.getPostLikesCount(postId).collect { count ->  // ✅ FEED REPOSITORY
                _likeCounts.update { it + (postId to count) }
                Log.d("FeedVM", "Like count for $postId: $count")
            }
        }
    }

    // ✅ YE NAYA FUNCTION HAI - Comment count کے لیے
    private fun fetchPostCommentCount(postId: String) {
        if (postCommentCountListeners[postId] == true) {
            Log.d("FeedVM", "Comment listener already active for $postId, skipping...")
            return
        }

        postCommentCountListeners[postId] = true

        viewModelScope.launch {
            feedRepository.getPostCommentsCount(postId).collect { count ->
                _commentCounts.update { it + (postId to count) }
                Log.d("FeedVM", "Comment count for $postId: $count")
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

    fun getLikeCount(postId: String): Int {
        val count = _likeCounts.value[postId] ?: 0
        Log.d("FeedVM", "Getting like count for $postId: $count")
        return count
    }

    fun isPostLiked(postId: String): Boolean = _likeStatus.value[postId] ?: false

    // ✅ YE NAYA FUNCTION HAI
    fun getCommentCount(postId: String): Int {
        val count = _commentCounts.value[postId] ?: 0
        Log.d("FeedVM", "Getting comment count for $postId: $count")
        return count
    }
}