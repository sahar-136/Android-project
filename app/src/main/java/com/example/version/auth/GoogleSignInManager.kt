package com.example.version.auth

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.example.version.R

class GoogleSignInManager(
    private val context: Context,
    private val onSignInResult: (String?) -> Unit
) {
    // FIXED: Correct Identity import
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)

    private val signInRequest = BeginSignInRequest.builder()
        .setPasswordRequestOptions(
            BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build()
        )
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                // FIXED: Using string resource instead of hardcoded value
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(true)
        .build()

    private var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    fun setActivityResultLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        activityResultLauncher = launcher
    }

    fun startSignIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(
                        result.pendingIntent.intentSender
                    ).build()
                    activityResultLauncher?.launch(intentSenderRequest)
                } catch (e: Exception) {
                    onSignInResult(null)
                }
            }
            .addOnFailureListener {
                onSignInResult(null)
            }
    }

    fun handleSignInResult(resultCode: Int, data: android.content.Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                onSignInResult(idToken)
            } catch (e: ApiException) {
                onSignInResult(null)
            }
        } else {
            onSignInResult(null)
        }
    }
}

@Composable
fun rememberGoogleSignInManager(
    onSignInResult: (String?) -> Unit
): GoogleSignInManager {
    val context = LocalContext.current
    return remember {
        GoogleSignInManager(context, onSignInResult)
    }
}