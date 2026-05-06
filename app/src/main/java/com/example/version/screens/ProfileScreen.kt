package com.example.version.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.version.models.User
import com.example.version.models.Post
import com.example.version.ui.theme.AppColors
import com.example.version.util.Resource
import com.example.version.viewmodel.ProfileViewModel
import com.example.version.viewmodel.DeletePostViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    deletePostViewModel: DeletePostViewModel = hiltViewModel(),
    onEditProfile: () -> Unit
) {
    val userState by profileViewModel.userState.collectAsState()
    val posts by profileViewModel.userPosts.collectAsState()
    val deleteState by deletePostViewModel.deleteState.collectAsState()

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }
    val isOwnProfile = currentUserId.isNotBlank() && currentUserId == userId

    // Delete state tracking
    var selectedPostForDelete by remember { mutableStateOf<Post?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var updatedPosts by remember { mutableStateOf(posts) }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            profileViewModel.loadUserProfile(userId)
        }
    }

    // ✅ OBSERVE DELETE STATE
    LaunchedEffect(deleteState) {
        when (deleteState) {
            is Resource.Success -> {
                // Remove deleted post from list
                updatedPosts = updatedPosts.filter { it.id != selectedPostForDelete?.id }
                selectedPostForDelete = null
                deletePostViewModel.resetDeleteState()
            }
            is Resource.Error -> {
                // Show error
                selectedPostForDelete = null
                deletePostViewModel.resetDeleteState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isOwnProfile) "You" else "Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = AppColors.BlackText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.BlackText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.PrimaryOrange
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundWhite)
                .padding(paddingValues)
        ) {
            when (val state = userState) {
                is Resource.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.PrimaryOrange)
                    }
                }

                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            state.message ?: "Could not load profile",
                            color = AppColors.ErrorRed
                        )
                    }
                }

                is Resource.Success -> {
                    val user: User = state.data ?: User()

                    // Update posts list when it changes
                    LaunchedEffect(posts) {
                        updatedPosts = posts
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp, bottom = 16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (user.profileImageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = user.profileImageUrl,
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Text(
                                            text = user.name.take(1).ifBlank { "?" }.uppercase(),
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.PrimaryOrange
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    "@${user.username}",
                                    color = AppColors.PrimaryOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    user.name,
                                    color = AppColors.BlackText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 22.sp
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                if (user.bio.isNotBlank()) {
                                    Text(
                                        user.bio,
                                        color = AppColors.TextGray,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(horizontal = 32.dp),
                                        lineHeight = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    "📷 ${updatedPosts.size} Posts",
                                    color = AppColors.BlackText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // ✅ Edit button ONLY for own profile
                                if (isOwnProfile) {
                                    Button(
                                        onClick = onEditProfile,
                                        modifier = Modifier
                                            .height(48.dp)
                                            .fillMaxWidth(0.8f),
                                        shape = RoundedCornerShape(30.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = AppColors.PrimaryOrange
                                        ),
                                        border = BorderStroke(2.dp, AppColors.PrimaryOrange)
                                    ) {
                                        Text("Edit Profile", fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))
                                }
                            }
                        }

                        item {
                            Text(
                                if (isOwnProfile) "My Posts" else "Posts",
                                color = AppColors.BlackText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        items(updatedPosts) { post: Post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                if (isOwnProfile) {
                                                    selectedPostForDelete = post
                                                    showDeleteConfirmation = true
                                                }
                                            }
                                        )
                                    },
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = post.caption,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        color = AppColors.BlackText
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (post.photoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = post.photoUrl,
                                            contentDescription = "Post Image",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = post.uploadTimestamp.toDate().toString(),
                                        fontSize = 12.sp,
                                        color = AppColors.TextGray
                                    )
                                }
                            }
                        }
                    }
                }

                else -> Unit
            }
        }
    }

    // ✅ DELETE CONFIRMATION DIALOG
    if (showDeleteConfirmation && selectedPostForDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Photo?") },
            text = { Text("This photo will be deleted permanently. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedPostForDelete?.let { post ->
                            deletePostViewModel.deletePost(
                                post.id,
                                post.userId,
                                post.photoUrl
                            )
                        }
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = AppColors.ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}