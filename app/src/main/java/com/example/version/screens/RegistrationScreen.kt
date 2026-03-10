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
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.version.auth.rememberGoogleSignInManager
import com.example.version.viewmodel.AuthViewModel
import com.example.version.util.Resource
import com.example.version.ui.theme.AppColors

@Composable
fun RegistrationScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val registerState by viewModel.registerState.observeAsState()
    val googleLoginState by viewModel.googleLoginState.observeAsState()
    val isUsernameAvailable by viewModel.isUsernameAvailable.observeAsState(null)

    // Google Sign-In Implementation
    var showUsernameDialog by remember { mutableStateOf(false) }
    var googleIdTokenPending by remember { mutableStateOf<String?>(null) }
    var googleUsername by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var googleUiError by remember { mutableStateOf<String?>(null) }

    // Username real-time check
    LaunchedEffect(username) {
        if (username.isNotBlank()) viewModel.checkUsernameAvailable(username)
    }

    // Google Sign-In Manager
    val googleSignInManager = rememberGoogleSignInManager(
        onSignInResult = { idToken ->
            if (idToken.isNullOrBlank()) {
                googleUiError = "Google sign-in failed: empty token."
                return@rememberGoogleSignInManager
            }

            googleIdTokenPending = idToken
            googleUsername = ""
            usernameError = null
            showUsernameDialog = true
        },
        onError = { msg ->
            googleUiError = msg
        }
    )

    // Activity Result Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        googleSignInManager.handleSignInResult(result.resultCode, result.data)
    }

    LaunchedEffect(Unit) {
        googleSignInManager.setActivityResultLauncher(launcher)
    }

    // Username Dialog for Google Registration
    if (showUsernameDialog) {
        AlertDialog(
            onDismissRequest = {
                showUsernameDialog = false
                googleIdTokenPending = null
            },
            title = { Text("Choose a username", color = AppColors.BlackText, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = googleUsername,
                        onValueChange = {
                            googleUsername = it
                            usernameError = null
                        },
                        label = { Text("Username", color = AppColors.TextGray) },
                        singleLine = true,
                        isError = usernameError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.PrimaryOrange,
                            focusedLabelColor = AppColors.PrimaryOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (usernameError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(usernameError!!, color = AppColors.ErrorRed, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This will be your unique username for SnapQuest.",
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

                        viewModel.loginWithGoogle(token, cleaned)
                        showUsernameDialog = false
                        googleIdTokenPending = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryOrange)
                ) { Text("Create Account", color = AppColors.ButtonTextWhite) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUsernameDialog = false
                        googleIdTokenPending = null
                    }
                ) { Text("Cancel", color = AppColors.TextGray) }
            },
            containerColor = AppColors.BackgroundWhite
        )
    }

    // MAIN LAYOUT: WHITE BACKGROUND
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundWhite) // WHITE SCREEN BACKGROUND
    ) {
        //DARK ORANGE TOP BAR - BLACK ARROW & TEXT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.PrimaryOrange) // DARK ORANGE TOP BAR
                .statusBarsPadding() // Handle status bar
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp) // Proper touch target
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.BlackText, //BLACK BACK ARROW
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp)) // Reduced spacing
                Text(
                    "Create Account",
                    fontSize = 20.sp, // Slightly smaller
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.BlackText // BLACK TEXT
                )
            }
        }

        // SCROLLABLE CONTENT - WHITE BACKGROUND
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp), // Proper padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name", color = AppColors.TextGray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = AppColors.TextGray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = null,
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

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = AppColors.TextGray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.AlternateEmail,
                        contentDescription = null,
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // Username availability indicator
            when (isUsernameAvailable) {
                true -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = AppColors.SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Username available",
                            color = AppColors.SuccessGreen,
                            fontSize = 12.sp
                        )
                    }
                }
                false -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = null,
                            tint = AppColors.ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Username taken",
                            color = AppColors.ErrorRed,
                            fontSize = 12.sp
                        )
                    }
                }
                else -> {
                    Spacer(modifier = Modifier.height(14.dp)) // Consistent spacing
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = AppColors.TextGray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = AppColors.PrimaryOrange,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = AppColors.PrimaryOrange,
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
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = AppColors.TextGray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = AppColors.PrimaryOrange,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            if (showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle confirm password visibility",
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
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(28.dp))

            //SIGN UP BUTTON
            Button(
                onClick = {
                    if (password != confirmPassword) return@Button
                    if ((isUsernameAvailable == false) || username.isBlank() || name.isBlank() || !email.contains("@")) return@Button
                    viewModel.register(name, email, username, password)
                    focusManager.clearFocus()
                },
                enabled = isUsernameAvailable == true && name.isNotBlank() && email.isNotBlank() && username.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryOrange, // ✅ SAME AS TOP BAR
                    disabledContainerColor = AppColors.PrimaryOrange.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    "Sign Up",
                    color = AppColors.ButtonTextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Register state feedback
            when (registerState) {
                is Resource.Loading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.PrimaryOrange, // ✅ SAME AS TOP BAR
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Creating account...",
                            color = AppColors.TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
                is Resource.Error -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        (registerState as Resource.Error).message ?: "Something went wrong",
                        color = AppColors.ErrorRed,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
                is Resource.Success -> {
                    LaunchedEffect(key1 = true) { onRegisterSuccess() }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Or divider
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

            Spacer(modifier = Modifier.height(24.dp))

            // Google sign up button
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

            // Google UI Error Display
            if (googleUiError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = googleUiError!!,
                    color = AppColors.ErrorRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Google Login State Handling
            when (googleLoginState) {
                is Resource.Loading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.PrimaryOrange, // ✅ SAME AS TOP BAR
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Creating account with Google...",
                            color = AppColors.TextGray,
                            fontSize = 13.sp
                        )
                    }
                }
                is Resource.Error -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        (googleLoginState as Resource.Error).message ?: "Google sign-up failed",
                        color = AppColors.ErrorRed,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
                is Resource.Success -> {
                    LaunchedEffect(key1 = "googleRegisterSuccess") { onRegisterSuccess() }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Log in link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Already have an account? ",
                    color = AppColors.TextGray,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = onBackClick,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        "Log In",
                        color = AppColors.PrimaryOrange, // ✅ SAME AS TOP BAR
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp)) // Bottom padding
        }
    }
}