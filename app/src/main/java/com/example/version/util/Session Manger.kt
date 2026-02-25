package com.example.version.util

import com.google.firebase.auth.FirebaseAuth

class SessionManager(private val firebaseAuth: FirebaseAuth) {

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun getUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}