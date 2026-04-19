package com.example.version.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.version.models.Post
import com.example.version.ui.components.FeedPostCard
import com.example.version.ui.theme.AppColors
import com.example.version.util.Resource
import com.example.version.viewmodel.AuthViewModel
import com.example.version.viewmodel.FeedViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    feedViewModel: FeedViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {

    val feedState by feedViewModel.feedPosts.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "SnapQuest",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.BlackText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Filled.Menu, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Notifications, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.PrimaryOrange,
                    titleContentColor = AppColors.BlackText
                )
            )
        },

        bottomBar = {
            NavigationBar(containerColor = AppColors.BackgroundWhite) {

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate("home")
                    },
                    icon = { Icon(Icons.Filled.Home, null) },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate("search")
                    },
                    icon = { Icon(Icons.Filled.Search, null) },
                    label = { Text("Search") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        navController.navigate("upload")
                    },
                    icon = { Icon(Icons.Filled.Add, null) },
                    label = { Text("Upload") }
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        navController.navigate("profile")
                    },
                    icon = { Icon(Icons.Filled.Person, null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundWhite)
                .padding(paddingValues)
        ) {

            when (feedState) {

                is Resource.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.PrimaryOrange)
                    }
                }

                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            (feedState as Resource.Error).message ?: "Error",
                            color = AppColors.ErrorRed
                        )
                    }
                }

                is Resource.Success<*> -> {

                    val posts =
                        (feedState as Resource.Success<List<Post>>).data ?: emptyList()

                    if (posts.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No posts yet", textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn {
                            items(posts) { post ->
                                FeedPostCard(
                                    post = post,
                                    feedViewModel = feedViewModel,
                                    navController = navController
                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                Button(onClick = {
                    authViewModel.logout()
                    navController.navigate("login") { popUpTo(0) }
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Logout?") },
            text = { Text("Are you sure?") }
        )
    }
}

