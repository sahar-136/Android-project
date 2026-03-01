package com.example.version.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.Post
import com.example.version.repository.FeedRepository
import com.example.version.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _feedPosts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading)
    val feedPosts: StateFlow<Resource<List<Post>>> = _feedPosts.asStateFlow()

    init {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            feedRepository.getFeedPosts()
                .collect { result -> _feedPosts.value = result }
        }
    }
}