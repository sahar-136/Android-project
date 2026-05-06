package com.example.version.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.example.version.models.Notification
import com.example.version.models.Post
import com.example.version.navigation.Routes
import com.example.version.ui.components.FeedPostCard
import com.example.version.ui.components.NotificationBell
import com.example.version.ui.components.NotificationDropdown
import com.example.version.ui.theme.AppColors
import com.example.version.util.Resource
import com.example.version.viewmodel.AuthViewModel
import com.example.version.viewmodel.FeedViewModel
import com.example.version.viewmodel.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    feedViewModel: FeedViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val feedState by feedViewModel.feedPosts.collectAsState()
    val trendingState by feedViewModel.trendingPosts.collectAsState()

    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    // ✅ Load notifications
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            notificationViewModel.loadNotifications(currentUserId)
            notificationViewModel.watchUnreadCount(currentUserId)
        }
    }

    // ✅ Load trending once
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            feedViewModel.loadTrendingOnce()
        }
    }

    Scaffold(
        topBar = {
            Column {

                // 🔝 Top Bar
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "SnapQuest",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout"
                            )
                        }
                    },
                    actions = {
                        NotificationBell(
                            unreadCount = unreadCount,
                            onClick = { showNotifications = !showNotifications }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppColors.PrimaryOrange
                    )
                )

                // 🔻 Tabs
                SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
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
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            val state = if (selectedTabIndex == 0) feedState else trendingState

            when (state) {

                is Resource.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message ?: "Error")
                    }
                }

                is Resource.Success<*> -> {
                    val posts = (state as Resource.Success<List<Post>>).data ?: emptyList()

                    if (posts.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No posts")
                        }
                    } else {
                        LazyColumn {
                            items(posts, key = { it.id }) { post ->
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

            // 🔔 Notifications dropdown
            if (showNotifications) {

                val notificationsList = when (notifications) {
                    is Resource.Success -> {
                        (notifications as Resource.Success<List<Notification>>).data ?: emptyList()
                    }
                    else -> emptyList()
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(AppColors.BlackText.copy(alpha = 0.5f))
                        .clickable { showNotifications = false }
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 60.dp, end = 8.dp)
                    ) {
                        NotificationDropdown(
                            notifications = notificationsList,
                            onNotificationClick = { postId, id ->
                                notificationViewModel.markAsRead(id)
                                if (postId.isNotBlank()) {
                                    navController.navigate(Routes.photoDetails(postId))
                                }
                                showNotifications = false
                            },
                            onClose = { showNotifications = false }
                        )
                    }
                }
            }
        }
    }

    // 🔥 Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                Button(onClick = {
                    authViewModel.logout()
                    showLogoutDialog = false
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") }
        )
    }
}