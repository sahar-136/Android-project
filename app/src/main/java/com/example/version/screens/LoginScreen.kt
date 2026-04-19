package com.example.version.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.version.auth.rememberGoogleSignInManager
import com.example.version.ui.theme.AppColors
import com.example.version.util.Resource
import com.example.version.viewmodel.AuthViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSignUpClick: () -> Unit,
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val loginState by viewModel.loginState.observeAsState()
    val googleLoginState by viewModel.googleLoginState.observeAsState()

    // Username availability state (re-used for Google username prompt)
    val isUsernameAvailable by viewModel.isUsernameAvailable.observeAsState(null)

    // For username prompt (needed for first-time Google sign-in only)
    var showUsernameDialog by remember { mutableStateOf(false) }
    var googleIdTokenPending by remember { mutableStateOf<String?>(null) }
    var googleUsername by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }

    // UI error for Google flow
    var googleUiError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    var usernameCheckJob by remember { mutableStateOf<Job?>(null) }

    fun resetGoogleDialogState() {
        showUsernameDialog = false
        googleUsername = ""
        usernameError = null
    }

    // Google One Tap manager
    // IMPORTANT CHANGE:
    // - Do NOT open username dialog immediately.
    // - First try loginWithGoogle(token, null).
    val googleSignInManager = rememberGoogleSignInManager(
        onSignInResult = { idToken ->
            if (idToken.isNullOrBlank()) {
                googleUiError = "Google sign-in failed: empty token."
                return@rememberGoogleSignInManager
            }
            googleUiError = null
            googleIdTokenPending = idToken
            resetGoogleDialogState()

            // Try sign-in first WITHOUT username.
            viewModel.loginWithGoogle(idToken, username = null)
        },
        onError = { msg ->
            googleUiError = msg
        }
    )

    // Launcher to receive One Tap result
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        googleSignInManager.handleSignInResult(result.resultCode, result.data)
    }

    LaunchedEffect(Unit) {
        googleSignInManager.setActivityResultLauncher(launcher)
    }

    // If repository says username is required, open dialog (first-time user only)
    LaunchedEffect(googleLoginState) {
        val msg = (googleLoginState as? Resource.Error)?.message.orEmpty()
        if (msg.contains("Username is required", ignoreCase = true)) {
            showUsernameDialog = true
        }
    }

    // Username dialog for Google users (only first-time)
    if (showUsernameDialog) {
        AlertDialog(
            onDismissRequest = {
                showUsernameDialog = false
                googleIdTokenPending = null
            },
            title = { Text("Choose a username", color = AppColors.BlackText) },
            text = {
                Column {
                    OutlinedTextField(
                        value = googleUsername,
                        onValueChange = { newValue ->
                            googleUsername = newValue
                            usernameError = null

                            // Real-time username check (debounced)
                            usernameCheckJob?.cancel()
                            usernameCheckJob = scope.launch {
                                delay(350)
                                val cleaned = googleUsername.trim()
                                if (cleaned.isNotBlank()) {
                                    viewModel.checkUsernameAvailable(cleaned)
                                }
                            }
                        },
                        label = { Text("Username") },
                        singleLine = true,
                        isError = usernameError != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    when (isUsernameAvailable) {
                        true -> Text("Username available", color = AppColors.SuccessGreen)
                        false -> Text("Username taken", color = AppColors.ErrorRed)
                        else -> Text(" ", color = AppColors.TextGray) // keeps spacing stable
                    }

                    if (usernameError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(usernameError!!, color = AppColors.ErrorRed)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Required only the first time you sign in with Google.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val token = googleIdTokenPending
                        if (token.isNullOrBlank()) {
                            showUsernameDialog = false
                            googleIdTokenPending = null
                            return@Button
                        }

                        val cleaned = googleUsername.trim()
                        if (cleaned.isBlank()) {
                            usernameError = "Username can't be empty."
                            return@Button
                        }

                        if (isUsernameAvailable == false) {
                            usernameError = "Username already taken."
                            return@Button
                        }

                        //  Now complete first-time registration with provided username
                        viewModel.loginWithGoogle(token, cleaned)
                        showUsernameDialog = false
                        googleIdTokenPending = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryOrange)
                ) { Text("Continue", color = AppColors.ButtonTextWhite) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUsernameDialog = false
                        googleIdTokenPending = null
                    }
                ) { Text("Cancel") }
            },
            containerColor = AppColors.BackgroundWhite
        )
    }

    //SCROLLABLE LAYOUT
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = AppColors.PrimaryOrange,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SnapQuest",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.BlackText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Welcome Back",
                fontSize = 16.sp,
                color = AppColors.TextGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = AppColors.TextGray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Email,
                        null,
                        tint = AppColors.PrimaryOrange,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.LightGray,
                    unfocusedContainerColor = AppColors.LightGray,
                    focusedBorderColor = AppColors.PrimaryOrange,
                    unfocusedBorderColor = AppColors.BorderGray,
                    focusedTextColor = AppColors.BlackText,
                    unfocusedTextColor = AppColors.BlackText,
                    focusedLabelColor = AppColors.PrimaryOrange,
                    unfocusedLabelColor = AppColors.TextGray
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = AppColors.TextGray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Lock,
                        null,
                        tint = AppColors.PrimaryOrange,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            null,
                            tint = AppColors.TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.LightGray,
                    unfocusedContainerColor = AppColors.LightGray,
                    focusedBorderColor = AppColors.PrimaryOrange,
                    unfocusedBorderColor = AppColors.BorderGray,
                    focusedTextColor = AppColors.BlackText,
                    unfocusedTextColor = AppColors.BlackText,
                    focusedLabelColor = AppColors.PrimaryOrange,
                    unfocusedLabelColor = AppColors.TextGray
                ),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Forgot Password?",
                        color = AppColors.PrimaryOrange,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.login(email, password)
                    focusManager.clearFocus()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryOrange),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(50.dp)
            ) {
                Text(
                    "Log In",
                    color = AppColors.ButtonTextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            when (loginState) {
                is Resource.Loading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.PrimaryOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Logging in...",
                            color = AppColors.TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
                is Resource.Error -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        (loginState as Resource.Error).message ?: "Something went wrong",
                        color = AppColors.ErrorRed,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
                is Resource.Success -> {
                    LaunchedEffect(key1 = true) { onLoginSuccess() }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(modifier = Modifier.weight(1f), color = AppColors.BorderGray)
                Text(
                    " Or ",
                    color = AppColors.TextGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Divider(modifier = Modifier.weight(1f), color = AppColors.BorderGray)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    googleUiError = null
                    googleSignInManager.startSignIn()
                },
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.CreamWhite,
                    contentColor = AppColors.BlackText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.BorderGray)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "G",
                        color = AppColors.GoogleRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Continue with Google",
                        color = AppColors.BlackText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (googleUiError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = googleUiError!!,
                    color = AppColors.ErrorRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            when (googleLoginState) {
                is Resource.Loading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.PrimaryOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Signing in with Google...",
                            color = AppColors.TextGray,
                            fontSize = 13.sp
                        )
                    }
                }
                is Resource.Error -> {
                    val msg = (googleLoginState as Resource.Error).message ?: ""
                    if (!msg.contains("Username is required", ignoreCase = true)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            msg.ifBlank { "Google sign-in failed" },
                            color = AppColors.ErrorRed,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is Resource.Success -> {
                    LaunchedEffect(key1 = "googleLoginSuccess") { onLoginSuccess() }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account? ",
                    color = AppColors.TextGray,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = onSignUpClick,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        "Sign up",
                        color = AppColors.PrimaryOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}