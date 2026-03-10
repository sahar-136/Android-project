package com.example.version.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.version.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException

class GoogleSignInManager(
    private val context: Context,
    private val onSignInResult: (String?) -> Unit,
    private val onError: (String) -> Unit = {}
) {
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)

    // IMPORTANT: Request ONLY Google ID token (do NOT request password)
    private val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(false) // optional: safer while debugging
        .build()

    private var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    fun setActivityResultLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        activityResultLauncher = launcher
    }

    fun startSignIn() {
        Log.d("GoogleSignIn", "startSignIn() called")
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(
                        result.pendingIntent.intentSender
                    ).build()
                    activityResultLauncher?.launch(intentSenderRequest)
                        ?: onError("Google Sign-In launcher is null.")
                } catch (e: Exception) {
                    Log.e("GoogleSignIn", "Launching One Tap failed", e)
                    onError(e.message ?: "Launching One Tap failed")
                }
            }
            .addOnFailureListener { e ->
                Log.e("GoogleSignIn", "beginSignIn failed", e)
                onError(e.message ?: "beginSignIn failed")
                onSignInResult(null)
            }
    }

    fun handleSignInResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken

                if (idToken.isNullOrBlank()) {
                    // Very useful for debugging:
                    Log.e(
                        "GoogleSignIn",
                        "Empty idToken. displayName=${credential.displayName}, id=${credential.id}"
                    )
                    onError("Google did not return an ID token. (Try again / check SHA-1 & Google provider)")
                    onSignInResult(null)
                    return
                }

                onSignInResult(idToken)
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "getSignInCredentialFromIntent failed", e)
                onError(e.message ?: "Reading sign-in result failed")
                onSignInResult(null)
            }
        } else {
            onError("Canceled")
            onSignInResult(null)
        }
    }
}

@Composable
fun rememberGoogleSignInManager(
    onSignInResult: (String?) -> Unit,
    onError: (String) -> Unit
): GoogleSignInManager {
    val context = LocalContext.current
    return remember {
        GoogleSignInManager(context, onSignInResult, onError)
    }
}