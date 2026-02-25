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
): ViewModel() {

    // Register state
    private val _registerState = MutableLiveData<Resource<User>>()
    val registerState: LiveData<Resource<User>> = _registerState

    // Login state
    private val _loginState = MutableLiveData<Resource<User>>()
    val loginState: LiveData<Resource<User>> = _loginState

    // Session state
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    // Register function
    fun register(name: String, email: String, password: String) {
        _registerState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.registerWithEmail(name, email, password)
            _registerState.value = result
            _isLoggedIn.value = (result is Resource.Success)
        }
    }

    // Login function
    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.loginWithEmail(email, password)
            _loginState.value = result
            _isLoggedIn.value = (result is Resource.Success)
        }
    }

    // Logout function
    fun logout() {
        authRepository.logout()
        _isLoggedIn.value = false
    }

    // Directly check session
    fun checkSession() {
        _isLoggedIn.value = authRepository.getCurrentUserId() != null
    }
}