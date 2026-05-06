package com.example.version.viewmodel

import androidx.lifecycle.LiveData
import android.util.Log
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

    private val _registerState = MutableLiveData<Resource<User>?>()
    val registerState: LiveData<Resource<User>?> = _registerState

    private val _loginState = MutableLiveData<Resource<User>?>()
    val loginState: LiveData<Resource<User>?> = _loginState

    private val _googleLoginState = MutableLiveData<Resource<User>?>()
    val googleLoginState: LiveData<Resource<User>?> = _googleLoginState

    private val _isUsernameAvailable = MutableLiveData<Boolean?>()
    val isUsernameAvailable: LiveData<Boolean?> = _isUsernameAvailable

    // ✅ CRITICAL: Single source of truth for session
    private val _isLoggedIn = MutableLiveData<Boolean>(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _resetState = MutableLiveData<Resource<Unit>?>()
    val resetState: LiveData<Resource<Unit>?> = _resetState

    init {
        // ✅ App launch par session check karo
        val currentUserId = authRepository.getCurrentUserId()
        Log.d("AuthVM_Init", "currentUserId: $currentUserId")

        if (currentUserId != null && currentUserId.isNotBlank()) {
            _isLoggedIn.value = true
            Log.d("AuthVM_Init", "✅ User already logged in: $currentUserId")
        } else {
            _isLoggedIn.value = false
            Log.d("AuthVM_Init", "❌ No user found - show login screen")
        }
    }

    fun register(name: String, email: String, username: String, password: String) {
        _registerState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.registerWithEmail(name, email, username, password)
            _registerState.value = result

            // ✅ Registration successful → update session IMMEDIATELY
            if (result is Resource.Success) {
                _isLoggedIn.value = true
                Log.d("AuthVM_Register", "✅ Registration success - isLoggedIn: TRUE")
            } else if (result is Resource.Error) {
                Log.d("AuthVM_Register", "❌ Registration failed: ${result.message}")
            }
        }
    }

    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.loginWithEmail(email, password)
            Log.d("AuthVM_Login", "result: $result")
            _loginState.value = result

            // ✅ Login successful → update session IMMEDIATELY
            if (result is Resource.Success) {
                _isLoggedIn.value = true
                Log.d("AuthVM_Login", "✅ Login success - isLoggedIn: TRUE")
            } else if (result is Resource.Error) {
                Log.d("AuthVM_Login", "❌ Login failed: ${result.message}")
            }
        }
    }

    fun loginWithGoogle(idToken: String, username: String? = null) {
        _googleLoginState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.loginWithGoogle(idToken, username)
            _googleLoginState.value = result

            // ✅ Google login successful → update session IMMEDIATELY
            if (result is Resource.Success) {
                _isLoggedIn.value = true
                Log.d("AuthVM_GoogleLogin", "✅ Google login success - isLoggedIn: TRUE")
            } else if (result is Resource.Error) {
                Log.d("AuthVM_GoogleLogin", "❌ Google login failed: ${result.message}")
            }
        }
    }

    fun checkUsernameAvailable(username: String) {
        viewModelScope.launch {
            _isUsernameAvailable.value = null
            _isUsernameAvailable.value = authRepository.isUsernameAvailable(username)
        }
    }

    fun sendPasswordReset(email: String) {
        _resetState.value = Resource.Loading
        viewModelScope.launch {
            _resetState.value = authRepository.sendPasswordResetEmail(email)
        }
    }

    fun logout() {
        authRepository.logout()
        _isLoggedIn.value = false  // ✅ CRITICAL: Update session IMMEDIATELY

        // ✅ Clear all states
        _registerState.value = null
        _loginState.value = null
        _googleLoginState.value = null
        _isUsernameAvailable.value = null
        _resetState.value = null

        Log.d("AuthVM_Logout", "✅ User logged out - isLoggedIn: FALSE")
    }
}