package com.example.version.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.version.models.User
import com.example.version.repository.AuthRepository
import com.example.version.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Register state
    private val _registerState = MutableLiveData<Resource<User>>()
    val registerState: LiveData<Resource<User>> = _registerState

    // Login state
    private val _loginState = MutableLiveData<Resource<User>>()
    val loginState: LiveData<Resource<User>> = _loginState

    // Google login state
    private val _googleLoginState = MutableLiveData<Resource<User>>()
    val googleLoginState: LiveData<Resource<User>> = _googleLoginState

    // Username availability state
    private val _isUsernameAvailable = MutableLiveData<Boolean?>()
    val isUsernameAvailable: LiveData<Boolean?> = _isUsernameAvailable

    // Session state
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    // Register with Email/Password (username included)
    fun register(name: String, email: String, username: String, password: String) {
        _registerState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.registerWithEmail(name, email, username, password)
            _registerState.value = result
            _isLoggedIn.value = (result is Resource.Success)
        }
    }

    // Login with Email/Password
    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.loginWithEmail(email, password)
            _loginState.value = result
            _isLoggedIn.value = (result is Resource.Success)
        }
    }

    // Google Sign-In (for new users, username is required)
    fun loginWithGoogle(idToken: String, username: String? = null) {
        _googleLoginState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.loginWithGoogle(idToken, username)
            _googleLoginState.value = result
            _isLoggedIn.value = (result is Resource.Success)
        }
    }

    // Username availability check (real-time)
    fun checkUsernameAvailable(username: String) {
        viewModelScope.launch {
            _isUsernameAvailable.value = null // reset while checking
            _isUsernameAvailable.value = authRepository.isUsernameAvailable(username)
        }
    }

    // Logout
    fun logout() {
        authRepository.logout()
        _isLoggedIn.value = false
    }

    // Session Check
    fun checkSession() {
        _isLoggedIn.value = authRepository.getCurrentUserId() != null
    }
}