package com.example.version.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.User
import com.example.version.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Debounced search pipeline
        viewModelScope.launch {
            _query
                .map { it.trim() }
                .debounce(350)
                .distinctUntilChanged()
                .collectLatest { q ->
                    if (q.isBlank()) {
                        _users.value = emptyList()
                        _loading.value = false
                    } else {
                        doSearch(q)
                    }
                }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    private fun doSearch(q: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _loading.value = true
            try {
                _users.value = searchRepository.searchUsers(q)
            } finally {
                _loading.value = false
            }
        }
    }
}