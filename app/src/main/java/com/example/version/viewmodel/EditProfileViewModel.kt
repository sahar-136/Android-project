package com.example.version.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.User
import com.example.version.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.version.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<User>>(Resource.Loading)
    val userState: StateFlow<Resource<User>> = _userState.asStateFlow()

    private val _editResult = MutableStateFlow<Resource<Boolean>?>(null)
    val editResult: StateFlow<Resource<Boolean>?> = _editResult.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _userState.value = Resource.Loading
            _userState.value = profileRepository.getCurrentUser()
        }
    }

    fun updateUserProfile(
        userId: String,
        name: String,
        username: String,
        bio: String
    ) {
        viewModelScope.launch {
            _editResult.value = Resource.Loading
            val currentUser = (_userState.value as? Resource.Success<User>)?.data
            _editResult.value = profileRepository.updateProfile(
                userId = userId,
                name = name,
                username = username,
                email = currentUser?.email ?: "", // usually email not editable
                bio = bio
            )
        }
    }

    fun resetEditResult() {
        _editResult.value = null
    }
    private val _profileImageState = MutableStateFlow<Resource<String>?>(null)
    val profileImageState: StateFlow<Resource<String>?> = _profileImageState.asStateFlow()

    fun updateProfileImage(userId: String, imageUri: Uri) {
        viewModelScope.launch {
            _profileImageState.value = Resource.Loading
            val result = profileRepository.updateProfileImage(userId, imageUri)
            _profileImageState.value = result
            // Optional: If update succeeded, reload user profile
            if (result is Resource.Success) {
                loadUserProfile()
            }
        }
    }
}