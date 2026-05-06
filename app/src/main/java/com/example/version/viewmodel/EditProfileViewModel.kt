package com.example.version.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.User
import com.example.version.repository.ProfileRepository
import com.example.version.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<User>>(Resource.Loading)
    val userState: StateFlow<Resource<User>> = _userState.asStateFlow()

    private val _editResult = MutableStateFlow<Resource<Boolean>?>(null)
    val editResult: StateFlow<Resource<Boolean>?> = _editResult.asStateFlow()

    private val _profileImageState = MutableStateFlow<Resource<String>?>(null)
    val profileImageState: StateFlow<Resource<String>?> = _profileImageState.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _userState.value = Resource.Loading
            Log.d("EditProfileVM", "Loading user profile...")
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
            Log.d("EditProfileVM", "Updating profile: name=$name, username=$username")

            val currentUser = (_userState.value as? Resource.Success<User>)?.data
            _editResult.value = profileRepository.updateProfile(
                userId = userId,
                name = name,
                username = username,
                email = currentUser?.email ?: "",
                bio = bio
            )

            Log.d("EditProfileVM", "Profile update result: ${_editResult.value}")
        }
    }

    fun resetEditResult() {
        _editResult.value = null
    }

    // ✅ FIXED: Sequential execution - wait for image upload, THEN update profile
    fun updateProfileImageAndUserProfile(
        userId: String,
        imageUri: Uri,
        name: String,
        username: String,
        bio: String
    ) {
        viewModelScope.launch {
            _profileImageState.value = Resource.Loading
            Log.d("EditProfileVM", "Starting image upload for userId=$userId, uri=$imageUri")

            try {
                // ✅ Step 1: Upload image first
                val imageResult = profileRepository.updateProfileImage(userId, imageUri)
                Log.d("EditProfileVM", "Image upload result: $imageResult")
                _profileImageState.value = imageResult

                if (imageResult is Resource.Success) {
                    Log.d("EditProfileVM", "Image uploaded successfully, waiting for sync...")
                    // ✅ Step 2: Wait for Firestore sync (1.5 seconds)
                    delay(1500)

                    Log.d("EditProfileVM", "Now updating profile information...")
                    // ✅ Step 3: NOW update profile info (profileImageUrl already saved in Firestore)
                    _editResult.value = Resource.Loading
                    _editResult.value = profileRepository.updateProfile(
                        userId = userId,
                        name = name,
                        username = username,
                        email = (_userState.value as? Resource.Success<User>)?.data?.email ?: "",
                        bio = bio
                    )
                    Log.d("EditProfileVM", "Profile update complete: ${_editResult.value}")
                } else if (imageResult is Resource.Error) {
                    Log.e("EditProfileVM", "Image upload failed: ${imageResult.message}")
                    _editResult.value = Resource.Error("Image upload failed: ${imageResult.message}")
                }
            } catch (e: Exception) {
                Log.e("EditProfileVM", "Exception during image upload: ${e.message}", e)
                _profileImageState.value = Resource.Error(e.message ?: "Unknown error")
                _editResult.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ✅ For profile update without image change
    fun updateUserProfileOnly(
        userId: String,
        name: String,
        username: String,
        bio: String
    ) {
        viewModelScope.launch {
            _editResult.value = Resource.Loading
            Log.d("EditProfileVM", "Updating profile only: name=$name")

            val currentUser = (_userState.value as? Resource.Success<User>)?.data
            _editResult.value = profileRepository.updateProfile(
                userId = userId,
                name = name,
                username = username,
                email = currentUser?.email ?: "",
                bio = bio
            )

            Log.d("EditProfileVM", "Profile update result: ${_editResult.value}")
        }
    }
}
