package com.example.version.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.repository.DeletePostRepository
import com.example.version.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeletePostViewModel @Inject constructor(
    private val deletePostRepository: DeletePostRepository
) : ViewModel() {

    private val _deleteState = MutableStateFlow<Resource<Boolean>?>(null)
    val deleteState: StateFlow<Resource<Boolean>?> = _deleteState.asStateFlow()

    fun deletePost(postId: String, userId: String, photoUrl: String) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading
            Log.d("DeletePostVM", "Deleting post: $postId")

            val result = deletePostRepository.deletePost(postId, userId, photoUrl)
            _deleteState.value = result

            Log.d("DeletePostVM", "Delete result: $result")
        }
    }

    fun resetDeleteState() {
        _deleteState.value = null
    }
}