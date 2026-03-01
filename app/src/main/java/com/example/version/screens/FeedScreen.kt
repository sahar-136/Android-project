package com.example.version.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.version.models.Post
import com.example.version.ui.components.FeedPostCard
import com.example.version.util.Resource
import com.example.version.viewmodel.FeedViewModel
import com.example.version.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel

private val FeedBlue = Color(0xFF0A66C2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val feedViewModel: FeedViewModel = hiltViewModel()
    val feedState by feedViewModel.feedPosts.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // --- Main Scaffold ---
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Home", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = FeedBlue
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
            FloatingActionButton(
                onClick = { navController.navigate("upload") },
                containerColor = FeedBlue,
                contentColor = Color.White,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Upload")
            }
        },
        containerColor = Color(0xFFEEF3F8)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (feedState) {
                is Resource.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = FeedBlue
                )
                is Resource.Error -> Text(
                    (feedState as Resource.Error).message ?: "Error loading posts",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                is Resource.Success<*> -> {
                    val posts = (feedState as? Resource.Success<List<Post>>)?.data ?: emptyList()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        items(posts) { post ->
                            FeedPostCard(post)
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
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                ) {
                    Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}