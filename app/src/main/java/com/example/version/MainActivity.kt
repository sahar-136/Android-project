package com.example.version

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.version.ui.theme.VersionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VersionTheme {
                val authViewModel: AuthViewModel = viewModel()
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                currentScreen = "main"
                            },
                            onNavigateToRegister = {
                                currentScreen = "register"
                            }
                        )
                    }
                    "register" -> {
                        RegisterScreen(
                            authViewModel = authViewModel,
                            onRegistrationSuccess = {
                                currentScreen = "login"
                            },
                            onNavigateToLogin = {
                                currentScreen = "login"
                            }
                        )
                    }
                    "main" -> {
                        // Temporary main screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Welcome to SnapQuest!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    authViewModel.logout()
                                    currentScreen = "login"
                                }
                            ) {
                                Text("Logout")
                            }
                        }
                    }
                }
            }
        }
    }
}