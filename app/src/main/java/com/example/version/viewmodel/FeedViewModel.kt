package com.example.version.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.Post
import com.example.version.repository.AuthRepository
import com.example.version.repository.FeedRepository
import com.example.version.repository.LikeRepository
import com.example.version.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val likeRepository: LikeRepository,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore // ✅ Added for user snapshot listeners
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

    // Comment counts map (postId → count)
    private val _commentCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val commentCounts: StateFlow<Map<String, Int>> = _commentCounts.asStateFlow()

    // ✅ User profile URLs (userId -> profileImageUrl) from users collection (real-time)
    private val _userProfileUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val userProfileUrls: StateFlow<Map<String, String>> = _userProfileUrls.asStateFlow()

    // Track listeners
    private val postLikeCountListeners = mutableMapOf<String, Boolean>()
    private val postCommentCountListeners = mutableMapOf<String, Boolean>()

    // ✅ Track user listeners so we don’t create duplicates
    private val userListeners = mutableMapOf<String, ListenerRegistration>()

    init {
        loadFeed()
    }

    override fun onCleared() {
        super.onCleared()
        // ✅ Remove user snapshot listeners to avoid leaks
        userListeners.values.forEach { it.remove() }
        userListeners.clear()
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

                        // ✅ Watch post owner's profile image in real-time (join)
                        watchUserProfileImage(post.userId)

                        fetchPostLikeStatus(post.id)
                        fetchPostLikesCount(post.id)
                        fetchPostCommentCount(post.id)
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

                        // ✅ Watch post owner's profile image in real-time (join)
                        watchUserProfileImage(post.userId)

                        fetchPostLikeStatus(post.id)
                        fetchPostLikesCount(post.id)
                        fetchPostCommentCount(post.id)
                    }
                }
            }
        }
    }

    // ✅ Real-time user doc listener (userId -> profileImageUrl)
    private fun watchUserProfileImage(userId: String) {
        if (userId.isBlank()) return
        if (userListeners.containsKey(userId)) return

        val registration = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FeedVM-User", "Error listening user $userId: ${error.message}")
                    return@addSnapshotListener
                }

                val url = snapshot?.getString("profileImageUrl").orEmpty()
                _userProfileUrls.update { it + (userId to url) }
                Log.d("FeedVM-User", "Updated profileImageUrl for $userId = '$url'")
            }

        userListeners[userId] = registration
    }

    private fun fetchPostLikeStatus(postId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val isLiked = likeRepository.isPostLikedByUser(postId, userId)
            _likeStatus.update { it + (postId to isLiked) }
            Log.d("FeedVM", "Like status for $postId: $isLiked")
        }
    }

    private fun fetchPostLikesCount(postId: String) {
        if (postLikeCountListeners[postId] == true) {
            Log.d("FeedVM", "Like listener already active for $postId, skipping...")
            return
        }

        postLikeCountListeners[postId] = true

        viewModelScope.launch {
            feedRepository.getPostLikesCount(postId).collect { count ->
                _likeCounts.update { it + (postId to count) }
                Log.d("FeedVM", "Like count for $postId: $count")
            }
        }
    }

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

    fun getLikeCount(postId: String): Int = _likeCounts.value[postId] ?: 0
    fun isPostLiked(postId: String): Boolean = _likeStatus.value[postId] ?: false
    fun getCommentCount(postId: String): Int = _commentCounts.value[postId] ?: 0
}