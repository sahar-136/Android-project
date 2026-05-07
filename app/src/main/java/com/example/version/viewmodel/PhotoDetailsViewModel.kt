package com.example.version.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.Post
import com.example.version.repository.AuthRepository
import com.example.version.repository.PhotoRepository
import com.example.version.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoDetailsViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore // ✅ for user snapshot listener
) : ViewModel() {

    private val _post = MutableStateFlow<Resource<Post>>(Resource.Loading)
    val post: StateFlow<Resource<Post>> = _post.asStateFlow()

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _likesCount = MutableStateFlow(0)
    val likesCount: StateFlow<Int> = _likesCount.asStateFlow()

    // ✅ user profile image url for header (real-time)
    private val _userProfileImageUrl = MutableStateFlow("")
    val userProfileImageUrl: StateFlow<String> = _userProfileImageUrl.asStateFlow()

    private var userListener: ListenerRegistration? = null
    private var currentUserIdListening: String? = null

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        userListener = null
    }

    fun loadPostDetails(postId: String) {
        viewModelScope.launch {
            photoRepository.getPostById(postId).collect { result ->
                _post.value = result

                // ✅ Once we have the post, start/refresh user listener
                val postUserId = (result as? Resource.Success<Post>)?.data?.userId.orEmpty()
                watchUserProfileImage(postUserId)
            }
        }
    }

    private fun watchUserProfileImage(userId: String) {
        if (userId.isBlank()) return

        // avoid re-registering same listener
        if (currentUserIdListening == userId && userListener != null) return
        currentUserIdListening = userId

        userListener?.remove()
        userListener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PhotoDetailsVM-User", "User listen error: ${error.message}")
                    return@addSnapshotListener
                }
                val url = snapshot?.getString("profileImageUrl").orEmpty()
                _userProfileImageUrl.value = url
                Log.d("PhotoDetailsVM-User", "profileImageUrl updated: '$url'")
            }
    }

    fun loadLikeStatus(postId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _isLiked.value = photoRepository.isPostLikedByUser(postId, userId)
        }
    }

    fun loadLikesCount(postId: String) {
        viewModelScope.launch {
            photoRepository.getPostLikesCount(postId).collect { count ->
                _likesCount.value = count
            }
        }
    }

    fun toggleLike(postId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val result = photoRepository.toggleLike(postId, userId)
            if (result is Resource.Success) {
                _isLiked.value = result.data
                loadLikesCount(postId)
            }
        }
    }
}