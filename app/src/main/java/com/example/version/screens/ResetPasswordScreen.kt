package com.example.version.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.version.viewmodel.AuthViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.version.ui.theme.AppColors
import com.example.version.util.Resource
import kotlinx.coroutines.delay

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val resetState by viewModel.resetState.observeAsState()

    // ✅ Auto-redirect after success
    LaunchedEffect(resetState) {
        if (resetState is Resource.Success) {
            // Show success for 2 seconds, then go back
            delay(2000)
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundWhite)
    ) {
        // ✅ ORANGE HEADER BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.PrimaryOrange)
                .padding(vertical = 16.dp, horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.BlackText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    "Reset Password",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.BlackText
                )
            }
        }

        // ✅ SCROLLABLE CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center  // ✅ CENTER content
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            // ✅ EMAIL INPUT FIELD
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Registered Email", color = AppColors.TextGray, fontSize = 14.sp) },
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

            Spacer(modifier = Modifier.height(28.dp))

            // ✅ SEND RESET LINK BUTTON - ORANGE
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        viewModel.sendPasswordReset(email)
                    }
                },
                enabled = email.isNotBlank() && resetState !is Resource.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryOrange,
                    disabledContainerColor = Color(0xFFFFCC80)
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                when (resetState) {
                    is Resource.Loading -> {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = AppColors.ButtonTextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Sending...",
                                color = AppColors.ButtonTextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    else -> {
                        Text(
                            "Send Reset Link",
                            color = AppColors.ButtonTextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ✅ STATUS MESSAGES
            when (resetState) {
                is Resource.Loading -> {
                    // Handled in button
                }
                is Resource.Success -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFFFE0B2),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ Success!",
                                color = AppColors.PrimaryOrange,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Check your email for the password reset link",
                                color = Color(0xFFE65100),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFFFCDD2),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✗ Error",
                                color = AppColors.ErrorRed,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (resetState as Resource.Error).message
                                    ?: "Failed to send reset email",
                                color = Color(0xFFB71C1C),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}