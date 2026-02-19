package com.example.version

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.version.ui.theme.*

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()

    // Handle login success
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess && authState.currentUser != null) {
            onLoginSuccess()
        }
    }

    // Simple White Background
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // SnapQuest Logo
        Text(
            text = "📸 SnapQuest",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = LinkedInBlue,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Text(
            text = "Welcome Back",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Show error message if any
        if (authState.isError != null) {
            Text(
                text = authState.isError!!,
                color = ErrorRed,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (authState.isError != null) {
                    authViewModel.clearError()
                }
            },
            placeholder = { Text("Email", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = "Email",
                    tint = LinkedInBlue
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            enabled = !authState.isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LinkedInBlue,
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (authState.isError != null) {
                    authViewModel.clearError()
                }
            },
            placeholder = { Text("Password", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Password",
                    tint = LinkedInBlue
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true,
            enabled = !authState.isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LinkedInBlue,
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Forgot Password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {}) {
                Text("Forgot password?", color = LinkedInBlue)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                authViewModel.login(email.trim(), password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !authState.isLoading && email.isNotEmpty() && password.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = LinkedInBlue),
            shape = RoundedCornerShape(26.dp)
        ) {
            if (authState.isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logging in...", color = Color.White)
                }
            } else {
                Text("Log in", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Or", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Google Button
        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            enabled = !authState.isLoading
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("G", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Link
        Row {
            Text("Don't have an account? ", color = Color.Gray)
            TextButton(
                onClick = onNavigateToRegister,
                enabled = !authState.isLoading
            ) {
                Text("Sign up", color = LinkedInBlue)
            }
        }
    }
}