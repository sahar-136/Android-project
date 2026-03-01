package com.example.version.repository

import com.example.version.models.User
import com.example.version.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    //============ EMAIL REGISTRATION WITH USERNAME ============//
    override suspend fun registerWithEmail(
        name: String,
        email: String,
        username: String,
        password: String
    ): Resource<User> {
        return try {
            // Check username availability first!
            if (!isUsernameAvailable(username)) {
                return Resource.Error("Username already taken")
            }
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser == null) {
                Resource.Error("Firebase user creation failed")
            } else {
                val user = User(
                    userId = firebaseUser.uid,
                    name = name,
                    email = email,
                    username = username
                )
                firestore.collection("users").document(user.userId).set(user).await()
                Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    //============ EMAIL LOGIN ============//
    override suspend fun loginWithEmail(
        email: String, password: String
    ): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser == null) {
                Resource.Error("Firebase user not found")
            } else {
                val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    Resource.Success(user)
                } else {
                    Resource.Error("User document not found in Firestore")
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    //============ GOOGLE LOGIN (with username for NEW users) ============//
    override suspend fun loginWithGoogle(
        idToken: String,
        username: String?
    ): Resource<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            if (firebaseUser == null) {
                Resource.Error("Google sign-in failed")
            } else {
                val docRef = firestore.collection("users").document(firebaseUser.uid)
                val doc = docRef.get().await()
                val user: User =
                    if (doc.exists()) {
                        doc.toObject(User::class.java)!!
                    } else {
                        // New Google user: Accept username (must not be null)
                        if (username.isNullOrBlank()) {
                            return Resource.Error("Username is required for first-time Google sign-in.")
                        }
                        // Check username available
                        if (!isUsernameAvailable(username)) {
                            return Resource.Error("Username already taken")
                        }
                        val newUser = User(
                            userId = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "",
                            email = firebaseUser.email ?: "",
                            username = username, // <-- username
                            profileImageUrl = firebaseUser.photoUrl?.toString() ?: ""
                        )
                        docRef.set(newUser).await()
                        newUser
                    }
                Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Google login failed")
        }
    }

    //============ USERNAME AVAILABILITY CHECK ============//
    override suspend fun isUsernameAvailable(username: String): Boolean {
        val query = firestore.collection("users")
            .whereEqualTo("username", username)
            .get().await()
        return query.isEmpty
    }

    //============ LOGOUT ============//
    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}