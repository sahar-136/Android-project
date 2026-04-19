package com.example.version.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.User
import com.example.version.models.Post
import com.example.version.repository.ProfileRepository
import com.example.version.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<User>>(Resource.Loading)
    val userState: StateFlow<Resource<User>> = _userState.asStateFlow()

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts.asStateFlow()

    fun loadCurrentUser() {
        viewModelScope.launch {
            _userState.value = Resource.Loading
            val result = profileRepository.getCurrentUser()
            _userState.value = result

            // On user success, also load posts for this userId
            if (result is Resource.Success) {
                result.data?.let { user ->
                    loadUserPosts(user.userId)
                }
            }
        }
    }

    fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            val posts = profileRepository.getPostsByUser(userId)
            _userPosts.value = posts
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _userState.value = Resource.Loading
            val result = profileRepository.getUserById(userId)
            _userState.value = result

            if (result is Resource.Success) {
                result.data?.let { user ->
                    loadUserPosts(user.userId)
                }
            }
        }
    }}