package com.example.version.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.version.models.Post
import com.example.version.ui.components.FeedPostCard
import com.example.version.util.Resource
import com.example.version.viewmodel.FeedViewModel
import com.example.version.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val feedViewModel: FeedViewModel = hiltViewModel()
    val feedState by feedViewModel.feedPosts.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ⭐ Deep Royal Purple Background - Same as Login/Register
    val royalDark = Color(0xFF14082B)
    val royalDeep = Color(0xFF2A0E4A)
    val royalRich = Color(0xFF3E1C6D)

    // ⭐ Soft Shine Glow (subtle)
    val royalGlow = Color(0xFF6A35B8).copy(alpha = 0.25f)

    // ⭐ Deep Peach Colors
    val deepPeachStart = Color(0xFFE8765C)
    val deepPeachEnd = Color(0xFFD45C47)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Deep Royal Purple Background
                Brush.verticalGradient(
                    colors = listOf(
                        royalRich,
                        royalDeep,
                        royalDark
                    )
                )
            )
    ) {
        // ✨ Deep Lines & Bubbles - Radial Gradient with Overlapping Effects
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            royalGlow,
                            Color.Transparent,
                            Color(0xFF4A2C85).copy(alpha = 0.15f) // Deep lines effect
                        ),
                        radius = 1200f
                    )
                )
        )

        // Royal Glow Effect - Subtle Background Shine
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF5B2C87).copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Home", color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent // Transparent to show background
                    ),
                    actions = {
                        IconButton(
                            onClick = { showLogoutDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                // Deep Peach FAB with Gradient
                FloatingActionButton(
                    onClick = { navController.navigate("upload") },
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp, end = 16.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(deepPeachStart, deepPeachEnd)
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Upload")
                }
            },
            containerColor = Color.Transparent // Transparent to show background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (feedState) {
                    is Resource.Loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                    is Resource.Error -> Text(
                        (feedState as Resource.Error).message ?: "Error loading posts",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    is Resource.Success<*> -> {
                        val posts = (feedState as? Resource.Success<List<Post>>)?.data ?: emptyList()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 16.dp)
                        ) {
                            items(posts) { post ->
                                // Simple FeedPostCard - No Extra Parameters
                                FeedPostCard(post = post)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        // --- Logout Confirmation Dialog ---
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout", color = Color.White) },
                text = { Text("Are you sure you want to logout?", color = Color.White) },
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
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier.background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Red, Color(0xFFE53E3E))
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                    ) {
                        Text("Logout", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = royalDeep,
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}