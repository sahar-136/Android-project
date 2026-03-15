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

    private val _registerState = MutableLiveData<Resource<User>>()
    val registerState: LiveData<Resource<User>> = _registerState


    private val _loginState = MutableLiveData<Resource<User>>()
    val loginState: LiveData<Resource<User>> = _loginState

    private val _googleLoginState = MutableLiveData<Resource<User>>()
    val googleLoginState: LiveData<Resource<User>> = _googleLoginState

    private val _isUsernameAvailable = MutableLiveData<Boolean?>()
    val isUsernameAvailable: LiveData<Boolean?> = _isUsernameAvailable

    // single source of truth for session
    private val _isLoggedIn = MutableLiveData<Boolean>(authRepository.getCurrentUserId() != null)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    init {
        // app open hote hi session check
        _isLoggedIn.value = authRepository.getCurrentUserId() != null
    }

    fun register(name: String, email: String, username: String, password: String) {
        _registerState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.registerWithEmail(name, email, username, password)
            _registerState.value = result
            _isLoggedIn.value = result is Resource.Success
        }
    }

    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.loginWithEmail(email, password)
            _loginState.value = result
            _isLoggedIn.value = result is Resource.Success
        }
    }

    fun loginWithGoogle(idToken: String, username: String? = null) {
        _googleLoginState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.loginWithGoogle(idToken, username)
            _googleLoginState.value = result
            _isLoggedIn.value = result is Resource.Success
        }
    }

    fun checkUsernameAvailable(username: String) {
        viewModelScope.launch {
            _isUsernameAvailable.value = null
            _isUsernameAvailable.value = authRepository.isUsernameAvailable(username)
        }
    }

    fun logout() {
        authRepository.logout()
        _isLoggedIn.value = false
    }
}