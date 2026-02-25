package com.example.version.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.version.viewmodel.AuthViewModel
import com.example.version.util.Resource
import com.example.version.ui.theme.*

@Composable
fun LoginScreen(
    onSignUpClick: () -> Unit,
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val loginState by viewModel.loginState.observeAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(colors = listOf(BlueStart, BlueMiddle, BlueEnd))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo (put your camera vector/drawable here)
            Icon(
                imageVector = Icons.Filled.Lock, // Camera vector recommended!
                contentDescription = "Logo",
                tint = ButtonTextWhite,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("SnapQuest", style = Typography.headlineLarge, color = ButtonTextWhite)
            Spacer(modifier = Modifier.height(36.dp))
            Text("Welcome Back", style = Typography.titleLarge, color = ButtonTextWhite)
            Spacer(modifier = Modifier.height(20.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().background(Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().background(Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(onClick = { /* TODO: Forgot Password logic */ }) {
                Text("Forgot Password?", style = Typography.labelSmall, color = ButtonTextWhite)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.login(email, password)
                    focusManager.clearFocus()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlueMiddle),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Log in", color = ButtonTextWhite, style = Typography.labelLarge) }
            Spacer(modifier = Modifier.height(12.dp))

            // Show login errors/success/loading
            when (loginState) {
                is Resource.Loading -> CircularProgressIndicator()
                is Resource.Error -> Text(
                    (loginState as Resource.Error).message ?: "Something went wrong",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 6.dp)
                )
                is Resource.Success -> {
                    LaunchedEffect(key1 = true) { onLoginSuccess() }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Or divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.White)
                Text(" Or ", color = ButtonTextWhite, style = Typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp))
                Divider(modifier = Modifier.weight(1f), color = Color.White)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { /* Handle Google login */ },
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                // Placeholder icon
                Icon(Icons.Filled.Email, contentDescription = "Google", tint = BlueMiddle, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google", color = BlueMiddle, style = Typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(18.dp))
            TextButton(onClick = onSignUpClick) {
                Text("Don't have an account? Sign up", style = Typography.labelSmall, color = ButtonTextWhite)
            }
        }
    }
}