package com.example.version.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onEditProfile: (() -> Unit)? = null
) {
    val userState by profileViewModel.userState.collectAsState()
    val posts by profileViewModel.userPosts.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.loadCurrentUser()
    }

        LaunchedEffect(key1 = userId) {
            profileViewModel.loadUserProfile(userId)
        }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = AppColors.BlackText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
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
            when (userState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.PrimaryOrange)
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (userState as Resource.Error).message ?: "Could not load profile",
                            color = AppColors.ErrorRed
                        )
                    }
                }
                is Resource.Success -> {
                    val user = (userState as Resource.Success<User>).data
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
                                            text = user.name.take(1).uppercase(),
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
                                    "📷 ${posts.size} Posts",
                                    color = AppColors.BlackText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = { onEditProfile?.invoke() },
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
                        item {
                            Text(
                                "My Posts",
                                color = AppColors.BlackText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                        items(posts) { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                else -> {}
            }
        }
    }
}