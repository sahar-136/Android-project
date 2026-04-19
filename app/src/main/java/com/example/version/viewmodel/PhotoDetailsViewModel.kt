package com.example.version.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.Post
import com.example.version.repository.AuthRepository
import com.example.version.repository.PhotoRepository
import com.example.version.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoDetailsViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _post = MutableStateFlow<Resource<Post>>(Resource.Loading)
    val post: StateFlow<Resource<Post>> = _post.asStateFlow()

    private val _isLiked = MutableStateFlow<Boolean>(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _likesCount = MutableStateFlow<Int>(0)
    val likesCount: StateFlow<Int> = _likesCount.asStateFlow()

    fun loadPostDetails(postId: String) {
        viewModelScope.launch {
            photoRepository.getPostById(postId)
                .collect { result ->
                    _post.value = result
                }
        }
    }

    fun loadLikeStatus(postId: String) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            val isLiked = photoRepository.isPostLikedByUser(postId, userId)
            _isLiked.value = isLiked
        }
    }

    fun loadLikesCount(postId: String) {
        viewModelScope.launch {
            photoRepository.getPostLikesCount(postId)
                .collect { count ->
                    _likesCount.value = count
                }
        }
    }

    fun toggleLike(postId: String) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            val result = photoRepository.toggleLike(postId, userId)

            if (result is Resource.Success) {
                val newLikeStatus = result.data
                _isLiked.value = newLikeStatus
                loadLikesCount(postId)
            }
        }
    }
}