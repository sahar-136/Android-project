package com.example.version.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ArrowBack
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
fun RegistrationScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel(),
    onGoogleSignUpRequest: () -> Unit // Inject this from parent/activity for Google sign-in intent
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
    val isUsernameAvailable by viewModel.isUsernameAvailable.observeAsState(null)

    // Username real-time check with debounce (optional for performance)
    LaunchedEffect(username) {
        if (username.isNotBlank()) viewModel.checkUsernameAvailable(username)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = listOf(BlueStart, BlueMiddle, BlueEnd)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, top = 34.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text("Create Account", style = Typography.headlineLarge, color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 90.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Username with availability indicator
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.AlternateEmail, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            when (isUsernameAvailable) {
                true -> Text("✓ Username available", color = Color(0xFF388E3C), modifier = Modifier.align(Alignment.Start))
                false -> Text("✗ Username taken", color = Color(0xFFD32F2F), modifier = Modifier.align(Alignment.Start))
                else -> {}
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
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
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle confirm password visibility"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = {
                    if (password != confirmPassword) return@Button
                    // Username check and min fields validation
                    if ((isUsernameAvailable == false) || username.isBlank() || name.isBlank() || !email.contains("@")) return@Button
                    viewModel.register(name, email, username, password)
                    focusManager.clearFocus()
                },
                enabled = isUsernameAvailable == true && name.isNotBlank() && email.isNotBlank() && username.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BlueMiddle),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Sign up", color = ButtonTextWhite, style = Typography.labelLarge)
            }

            // Register state feedback
            when (registerState) {
                is Resource.Loading -> CircularProgressIndicator()
                is Resource.Error -> Text(
                    (registerState as Resource.Error).message ?: "Something went wrong",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 6.dp)
                )
                is Resource.Success -> {
                    LaunchedEffect(key1 = true) { onRegisterSuccess() }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.White)
                Text(" Or ", color = ButtonTextWhite, style = Typography.labelSmall, modifier = Modifier.padding(start = 8.dp, end = 8.dp))
                Divider(modifier = Modifier.weight(1f), color = Color.White)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { onGoogleSignUpRequest() },
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Filled.Email, contentDescription = "Google", tint = BlueMiddle, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google", color = BlueMiddle, style = Typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(18.dp))
            TextButton(onClick = onBackClick) {
                Text("Already have an account? Sign in", style = Typography.labelSmall, color = ButtonTextWhite)
            }
        }
    }
}