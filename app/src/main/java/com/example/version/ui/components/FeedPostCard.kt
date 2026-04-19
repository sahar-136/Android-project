package com.example.version.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
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
import com.example.version.models.Post
import com.example.version.navigation.Routes
import com.example.version.ui.theme.AppColors
import com.example.version.viewmodel.FeedViewModel
import coil3.compose.AsyncImage
import com.google.firebase.Timestamp

fun formatTimestamp(timestamp: Timestamp?): String {
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

@Composable
fun FeedPostCard(
    post: Post,
    feedViewModel: FeedViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {

    // ❤️ LIKE STATE (correct hai - isko change nahi karna)
    val isLiked by remember(feedViewModel.likeStatus) {
        derivedStateOf { feedViewModel.isPostLiked(post.postId) }
    }

    val likeCount by remember(feedViewModel.likeCounts) {
        derivedStateOf { feedViewModel.getLikeCount(post.postId) }
    }

    // ❌ OLD CODE (REMOVE KIYA):
    // val commentCount by remember(feedViewModel.commentCounts) {
    //     derivedStateOf { feedViewModel.getCommentCount(post.postId) }
    // }

    // ✅ FIX:
    // Comment count ab direct "post.commentsCount" se ayega
    // kyun ke Firestore snapshot listener already latest data bhej raha hai

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.BackgroundWhite
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(modifier = Modifier.padding(12.dp)) {

            // USER HEADER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppColors.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.userName.take(1).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryOrange
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.BlackText
                    )
                    Text(
                        text = formatTimestamp(post.uploadTimestamp),
                        fontSize = 12.sp,
                        color = AppColors.TextGray
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { }) {
                    Text("⋮", fontSize = 16.sp, color = AppColors.TextGray)
                }
            }

            // IMAGE
            AsyncImage(
                model = post.photoUrl,
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (post.postId.isNotEmpty()) {
                            navController.navigate(Routes.photoDetails(post.postId))
                        }
                    },
                contentScale = ContentScale.Crop
            )

            // CAPTION
            if (post.caption.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = post.caption,
                    fontSize = 14.sp,
                    color = AppColors.TextGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ACTION ROW
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                // ❤️ LIKE BUTTON
                Row(verticalAlignment = Alignment.CenterVertically) {

                    IconButton(
                        onClick = {
                            feedViewModel.togglePostLike(post.postId)
                        }
                    ) {
                        Icon(
                            imageVector = if (isLiked)
                                Icons.Filled.Favorite
                            else
                                Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else AppColors.TextGray
                        )
                    }

                    Text(
                        text = "$likeCount",
                        fontSize = 14.sp,
                        color = AppColors.TextGray
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 💬 COMMENT BUTTON (FIXED)
                Row(verticalAlignment = Alignment.CenterVertically) {

                    IconButton(
                        onClick = {
                            if (post.postId.isNotEmpty()) {
                                navController.navigate(Routes.comments(post.postId))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = AppColors.TextGray
                        )
                    }

                    Text(
                        // ✅ FINAL FIX: Direct Firestore value (always correct & updated)
                        text = "${post.commentsCount}",
                        fontSize = 14.sp,
                        color = AppColors.TextGray
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // SHARE
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = AppColors.TextGray
                    )
                }
            }
        }
    }
}