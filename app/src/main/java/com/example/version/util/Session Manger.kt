package com.example.version.util

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null
    fun getUserId(): String? = firebaseAuth.currentUser?.uid
    fun logout() = firebaseAuth.signOut()
}