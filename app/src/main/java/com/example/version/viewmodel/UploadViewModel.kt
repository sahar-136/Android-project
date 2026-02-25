package com.example.version.viewmodel



import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.Post
import com.example.version.repository.UploadRepository
import com.example.version.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val uploadRepository: UploadRepository
): ViewModel() {

    private val _uploadState = MutableLiveData<Resource<Post>>()
    val uploadState: LiveData<Resource<Post>> = _uploadState

    fun uploadPhoto(userId: String, fileUri: Uri, caption: String?) {
        _uploadState.value = Resource.Loading
        viewModelScope.launch {
            val result = uploadRepository.uploadPhotoAndCreatePost(userId, fileUri, caption)
            _uploadState.value = result
        }
    }
}