package com.example.version.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.version.viewmodel.AuthViewModel
import com.example.version.util.Resource

@Composable
fun LoginScreen(
    onSignUpClick: () -> Unit,
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel(),
    onGoogleSignInRequest: () -> Unit,
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val loginState by viewModel.loginState.observeAsState()
    val googleLoginState by viewModel.googleLoginState.observeAsState()

    // ⭐ Deep Royal Purple Base
    val royalDark = Color(0xFF14082B)
    val royalDeep = Color(0xFF2A0E4A)
    val royalRich = Color(0xFF3E1C6D)

    // ⭐ Soft Shine Glow (subtle)
    val royalGlow = Color(0xFF6A35B8).copy(alpha = 0.25f)

    // ⭐ Deep Peach Button Gradient (Made Deeper)
    val deepPeachStart = Color(0xFFE8765C)  // Deeper peach start
    val deepPeachEnd = Color(0xFFD45C47)    // Deeper peach end

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        royalRich,
                        royalDeep,
                        royalDark
                    )
                )
            )
    ) {

        // ✨ Soft Radial Shine Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            royalGlow,
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SnapQuest",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("EMAIL", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Filled.Email, null, tint = Color.Gray)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("PASSWORD", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Filled.Lock, null, tint = Color.Gray)
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword)
                                Icons.Filled.Visibility
                            else
                                Icons.Filled.VisibilityOff,
                            null,
                            tint = Color.Gray
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                visualTransformation =
                    if (showPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Forgot Password Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { }) {
                    Text(
                        "Forgot Password?",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Deep Login Button
            Button(
                onClick = {
                    viewModel.login(email, password)
                    focusManager.clearFocus()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(deepPeachStart, deepPeachEnd)
                        ),
                        shape = RoundedCornerShape(25.dp)
                    )
            ) {
                Text(
                    "Log In",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Login State Feedback
            when (loginState) {
                is Resource.Loading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = Color.White)
                }
                is Resource.Error -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        (loginState as Resource.Error).message ?: "Something went wrong",
                        color = Color.Red
                    )
                }
                is Resource.Success -> {
                    LaunchedEffect(key1 = true) { onLoginSuccess() }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Or Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    " Or ",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // FIXED: Google Button - Always Enabled
            Button(
                onClick = {
                    onGoogleSignInRequest() // This should always work
                },
                enabled = true, // Explicitly set to true
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White, // Same color when disabled
                    disabledContentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    "G",
                    color = Color.Red,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    "Continue with Google",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Google Login State Feedback
            when (googleLoginState) {
                is Resource.Loading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = Color.White)
                    Text("Signing in with Google...", color = Color.White)
                }
                is Resource.Error -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        (googleLoginState as Resource.Error).message ?: "Google sign-in failed",
                        color = Color.Red
                    )
                }
                is Resource.Success -> {
                    LaunchedEffect(key1 = "googleLoginSuccess") {
                        onLoginSuccess()
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = onSignUpClick) {
                Text(
                    "Don't have an account? Sign up",
                    color = Color.White
                )
            }
        }
    }
}