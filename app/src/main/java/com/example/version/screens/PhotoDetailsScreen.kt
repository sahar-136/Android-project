package com.example.version.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.version.models.Post
import com.example.version.ui.theme.AppColors
import com.example.version.util.Resource
import com.example.version.viewmodel.PhotoDetailsViewModel
import com.google.firebase.Timestamp
import com.example.version.navigation.Routes
fun formatTimestampPhotoDetails(timestamp: Timestamp?): String {
    if (timestamp == null) return "just now"

    val uploadTime = timestamp.toDate().time
    val currentTime = System.currentTimeMillis()
    val diffMillis = currentTime - uploadTime

    return when {
        diffMillis < 60000 -> "just now"
        diffMillis < 3600000 -> "${diffMillis / 60000} min ago"
        diffMillis < 86400000 -> "${diffMillis / 3600000} h ago"
        diffMillis < 604800000 -> "${diffMillis / 86400000} d ago"
        else -> "${diffMillis / 604800000} w ago"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailsScreen(
    postId: String,
    navController: NavController,
    viewModel: PhotoDetailsViewModel = hiltViewModel()
) {
    val postState by viewModel.post.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val likesCount by viewModel.likesCount.collectAsState()

    LaunchedEffect(postId) {
        viewModel.loadPostDetails(postId)
        viewModel.loadLikeStatus(postId)
        viewModel.loadLikesCount(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Details") },
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
                    containerColor = AppColors.PrimaryOrange,
                    titleContentColor = AppColors.BlackText
                )
            )
        }
    ) { paddingValues ->
        when (postState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppColors.PrimaryOrange,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = AppColors.ErrorRed,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Unable to load post",
                            color = AppColors.BlackText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.PrimaryOrange
                            )
                        ) {
                            Text("Go Back", color = AppColors.ButtonTextWhite)
                        }
                    }
                }
            }

            is Resource.Success<*> -> {
                val post = (postState as Resource.Success<Post>).data

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.BackgroundWhite)
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // USER HEADER
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    post.userName.take(1).uppercase(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryOrange
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    post.userName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.BlackText
                                )
                                Text(
                                    formatTimestampPhotoDetails(post.uploadTimestamp),
                                    fontSize = 12.sp,
                                    color = AppColors.TextGray
                                )
                            }
                        }
                    }

                    // PHOTO
                    item {
                        AsyncImage(
                            model = post.photoUrl,
                            contentDescription = "Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // CAPTION
                    if (post.caption.isNotBlank()) {
                        item {
                            Text(
                                post.caption,
                                fontSize = 14.sp,
                                color = AppColors.BlackText,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // LIKE & COMMENTS BUTTONS
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    viewModel.toggleLike(postId)
                                }
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleLike(postId) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked)
                                            Icons.Filled.Favorite
                                        else
                                            Icons.Filled.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isLiked) Color.Red else AppColors.TextGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    "$likesCount likes",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.BlackText
                                )
                            }

                            Spacer(modifier = Modifier.width(24.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    navController.navigate(Routes.comments(postId))
                                }
                            ) {
                                IconButton(
                                    onClick = {
                                        navController.navigate(Routes.comments(postId))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.ChatBubbleOutline,
                                        contentDescription = "Comments",
                                        tint = AppColors.TextGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    "${post.commentsCount} comments",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.BlackText
                                )
                            }
                        }
                    }

                    // VIEW ALL COMMENTS BUTTON
                    item {
                        Button(
                            onClick = {
                                navController.navigate(Routes.comments(postId))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.PrimaryOrange
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                "View All Comments",
                                color = AppColors.ButtonTextWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            else -> {}
        }
    }
}