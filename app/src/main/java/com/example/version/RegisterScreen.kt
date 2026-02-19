package com.example.version

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.version.ui.theme.*

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()

    // Handle registration success
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess && authState.currentUser != null) {
            kotlinx.coroutines.delay(1500)
            onRegistrationSuccess()
        }
    }

    // Simple White Background
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {

        // Header with Back Arrow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateToLogin,
                enabled = !authState.isLoading
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = LinkedInBlue
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Show success or error message
            if (authState.isSuccess && authState.currentUser != null) {
                Text(
                    text = "Account created successfully! 🎉",
                    color = SuccessGreen,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else if (authState.isError != null) {
                Text(
                    text = authState.isError!!,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (authState.isError != null) {
                        authViewModel.clearError()
                    }
                },
                placeholder = { Text("Name", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Name", tint = LinkedInBlue)
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true,
                enabled = !authState.isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LinkedInBlue,
                    unfocusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            )

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
                    Icon(Icons.Default.Email, contentDescription = "Email", tint = LinkedInBlue)
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true,
                enabled = !authState.isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LinkedInBlue,
                    unfocusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    if (authState.isError != null) {
                        authViewModel.clearError()
                    }
                },
                placeholder = { Text("Username", color = Color.Gray) },
                leadingIcon = {
                    Text("@", color = LinkedInBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true,
                enabled = !authState.isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LinkedInBlue,
                    unfocusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Username availability
            if (username.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text("✓ Username available", color = UsernameAvailable, fontSize = 12.sp)
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

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
                    Icon(Icons.Default.Lock, contentDescription = "Password", tint = LinkedInBlue)
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true,
                enabled = !authState.isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LinkedInBlue,
                    unfocusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (authState.isError != null) {
                        authViewModel.clearError()
                    }
                },
                placeholder = { Text("Confirm Password", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Confirm Password", tint = LinkedInBlue)
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true,
                enabled = !authState.isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LinkedInBlue,
                    unfocusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Password match indicator
            if (password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    if (password == confirmPassword) {
                        Text("✓ Passwords match", color = SuccessGreen, fontSize = 12.sp)
                    } else {
                        Text("✗ Passwords don't match", color = ErrorRed, fontSize = 12.sp)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sign Up Button
            Button(
                onClick = {
                    authViewModel.register(email.trim(), password, confirmPassword, name.trim())
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !authState.isLoading && name.isNotEmpty() && email.isNotEmpty() &&
                        username.isNotEmpty() && password.isNotEmpty() &&
                        confirmPassword.isNotEmpty() && password == confirmPassword,
                colors = ButtonDefaults.buttonColors(containerColor = LinkedInBlue),
                shape = RoundedCornerShape(26.dp)
            ) {
                if (authState.isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating account...", color = Color.White)
                    }
                } else {
                    Text("Sign up", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Or", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Google Button
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                enabled = !authState.isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("G", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Link
            Row {
                Text("Already have an account? ", color = Color.Gray)
                TextButton(
                    onClick = onNavigateToLogin,
                    enabled = !authState.isLoading
                ) {
                    Text("Log in", color = LinkedInBlue)
                }
            }
        }
    }
}