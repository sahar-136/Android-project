package com.example.version.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.version.models.Post
import com.example.version.ui.components.FeedPostCard
import com.example.version.util.Resource
import com.example.version.viewmodel.FeedViewModel
import com.example.version.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.version.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    onBackToLogin: () -> Unit = {}, // ✅ NEW PARAMETER FOR BACK TO LOGIN
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val feedViewModel: FeedViewModel = hiltViewModel()
    val feedState by feedViewModel.feedPosts.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Clean layout with scrollable content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundWhite) // ✅ WHITE SCREEN BACKGROUND
    ) {
        // DARK ORANGE TOP BAR - BLACK ICONS & TEXT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.PrimaryOrange) // ✅ DARK ORANGE TOP BAR
                .statusBarsPadding() // Handle status bar
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                //  LEFT SIDE - BLACK BACK ARROW (DIRECT TO LOGIN)
                IconButton(
                    onClick = { onBackToLogin() }, // DIRECT TO LOGIN - NO DIALOG
                    modifier = Modifier.size(40.dp) // Proper touch target
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back to Login", // Updated description
                        tint = AppColors.BlackText, // BLACK BACK ARROW
                        modifier = Modifier.size(24.dp)
                    )
                }

                // CENTER - "FEED" TITLE
                Text(
                    "Feed", // CHANGED FROM "Home" TO "Feed"
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.BlackText // BLACK TEXT
                )

                // RIGHT SIDE - BLACK LOGOUT ICON (WITH CONFIRMATION)
                IconButton(
                    onClick = { showLogoutDialog = true }, // SHOWS CONFIRMATION DIALOG
                    modifier = Modifier.size(40.dp) // Proper touch target
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = AppColors.BlackText, // BLACK LOGOUT ICON
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // CONTENT AREA - WHITE BACKGROUND
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundWhite)
        ) {
            when (feedState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.PrimaryOrange,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Unable to load posts",
                                color = AppColors.TextGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                (feedState as Resource.Error).message ?: "Please try again later",
                                color = AppColors.TextGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                is Resource.Success<*> -> {
                    val posts = (feedState as? Resource.Success<List<Post>>)?.data ?: emptyList()

                    if (posts.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "No posts yet",
                                    color = AppColors.BlackText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Be the first to share a photo!",
                                    color = AppColors.TextGray,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { navController.navigate("upload") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.PrimaryOrange
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        "Upload Photo",
                                        color = AppColors.ButtonTextWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        // Posts list with proper padding for top bar
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 16.dp, // Space after top bar
                                bottom = 100.dp, // Space for FAB
                                start = 16.dp,
                                end = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(posts) { post ->
                                // Clean post card
                                FeedPostCard(
                                    post = post,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = AppColors.LightGray,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
                else -> {}
            }

            // FLOATING ACTION BUTTON - Orange
            FloatingActionButton(
                onClick = { navController.navigate("upload") },
                containerColor = AppColors.PrimaryOrange,
                contentColor = AppColors.ButtonTextWhite,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 24.dp)
                    .size(56.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Upload",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // LOGOUT CONFIRMATION DIALOG
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = {
                    Text(
                        "Logout",
                        color = AppColors.BlackText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to logout?",
                        color = AppColors.TextGray,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo(0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.ErrorRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Logout",
                            color = AppColors.ButtonTextWhite,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false }
                    ) {
                        Text(
                            "Cancel",
                            color = AppColors.PrimaryOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                containerColor = AppColors.BackgroundWhite,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}