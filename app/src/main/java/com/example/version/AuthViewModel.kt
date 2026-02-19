package com.example.version

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: String? = null,
    val currentUser: FirebaseUser? = null
)

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val currentUser = auth.currentUser
        _authState.value = _authState.value.copy(
            currentUser = currentUser,
            isSuccess = currentUser != null
        )
    }

    // Login Function
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = _authState.value.copy(
                isError = "Please fill all fields"
            )
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                isError = null
            )

            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                isSuccess = true,
                                currentUser = auth.currentUser,
                                isError = null
                            )
                        } else {
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                isSuccess = false,
                                isError = task.exception?.message ?: "Login failed"
                            )
                        }
                    }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    isError = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    // Register Function
    fun register(email: String, password: String, confirmPassword: String, name: String) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            _authState.value = _authState.value.copy(
                isError = "Please fill all fields"
            )
            return
        }

        if (password != confirmPassword) {
            _authState.value = _authState.value.copy(
                isError = "Passwords don't match"
            )
            return
        }

        if (password.length < 6) {
            _authState.value = _authState.value.copy(
                isError = "Password must be at least 6 characters"
            )
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                isError = null
            )

            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                                displayName = name
                            }

                            user?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener {
                                    _authState.value = _authState.value.copy(
                                        isLoading = false,
                                        isSuccess = true,
                                        currentUser = user,
                                        isError = null
                                    )
                                }
                        } else {
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                isSuccess = false,
                                isError = task.exception?.message ?: "Registration failed"
                            )
                        }
                    }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    isError = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    // Logout Function
    fun logout() {
        auth.signOut()
        _authState.value = AuthState()
    }

    // Clear Error
    fun clearError() {
        _authState.value = _authState.value.copy(isError = null)
    }
}