package com.example.version.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.version.models.Post
import com.example.version.ui.components.FeedPostCard
import com.example.version.ui.theme.AppColors
import com.example.version.util.Resource
import com.example.version.viewmodel.AuthViewModel
import com.example.version.viewmodel.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    feedViewModel: FeedViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val feedState by feedViewModel.feedPosts.collectAsState()
    val trendingState by feedViewModel.trendingPosts.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) } // 0=Feed, 1=Trending

    // load trending when user opens Trending tab first time
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            feedViewModel.loadTrendingOnce()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SnapQuest",
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

                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = AppColors.BackgroundWhite,
                    contentColor = AppColors.PrimaryOrange
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Feed") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Trending") }
                    )
                }
            }
        },
        containerColor = AppColors.BackgroundWhite
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundWhite)
                .padding(paddingValues)
        ) {
            val state = if (selectedTabIndex == 0) feedState else trendingState

            when (state) {
                is Resource.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.PrimaryOrange)
                    }
                }

                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message ?: "Something went wrong",
                            color = AppColors.ErrorRed
                        )
                    }
                }

                is Resource.Success<*> -> {
                    val posts = (state as Resource.Success<List<Post>>).data ?: emptyList()

                    if (posts.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (selectedTabIndex == 0) "No posts yet" else "No trending posts yet",
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            items(
                                items = posts,
                                key = { it.id }
                            ) { post ->
                                FeedPostCard(
                                    post = post,
                                    feedViewModel = feedViewModel,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                Button(onClick = {
                    authViewModel.logout()
                    showLogoutDialog = false
                    navController.navigate("login") { popUpTo(0) }
                }) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            },
            title = { Text("Logout?") },
            text = { Text("Are you sure?") }
        )
    }
}