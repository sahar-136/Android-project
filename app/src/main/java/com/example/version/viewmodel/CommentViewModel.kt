package com.example.version.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.Comment
import com.example.version.repository.AuthRepository
import com.example.version.repository.CommentRepository
import com.example.version.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _comments = MutableStateFlow<Resource<List<Comment>>>(Resource.Loading)
    val comments: StateFlow<Resource<List<Comment>>> = _comments.asStateFlow()

    private val _addCommentState = MutableStateFlow<Resource<Boolean>?>(null)
    val addCommentState: StateFlow<Resource<Boolean>?> = _addCommentState.asStateFlow()

    private val _deleteCommentState = MutableStateFlow<Resource<Boolean>?>(null)
    val deleteCommentState: StateFlow<Resource<Boolean>?> = _deleteCommentState.asStateFlow()

    fun loadCommentsForPost(postId: String) {
        if (postId.isEmpty()) {
            _comments.value = Resource.Error("Post ID is empty")
            return
        }
        viewModelScope.launch {
            try {
                commentRepository.getCommentsByPostId(postId)
                    .collect { result ->
                        _comments.value = result
                    }
            } catch (e: Exception) {
                _comments.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addComment(postId: String, commentText: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _addCommentState.value = Resource.Error("User not authenticated")
            return
        }
        val userName = "User" // TODO: Get from AuthRepository
        _addCommentState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val result = commentRepository.addComment(
                    postId = postId,
                    userId = userId,
                    userName = userName,
                    commentText = commentText
                )
                _addCommentState.value = result
                if (result is Resource.Success) {
                    loadCommentsForPost(postId)
                    _addCommentState.value = null
                }
            } catch (e: Exception) {
                _addCommentState.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        _deleteCommentState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val result = commentRepository.deleteComment(postId, commentId)
                _deleteCommentState.value = result
                if (result is Resource.Success) {
                    _comments.update { currentResource ->
                        if (currentResource is Resource.Success) {
                            val updatedComments = currentResource.data.filter { it.commentId != commentId }
                            Resource.Success(updatedComments)
                        } else {
                            currentResource
                        }
                    }
                    _deleteCommentState.value = null
                }
            } catch (e: Exception) {
                _deleteCommentState.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetStates() {
        _comments.value = Resource.Loading
        _addCommentState.value = null
        _deleteCommentState.value = null
    }
}