package com.example.version.ui.components

import androidx.compose.foundation.background
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
import com.example.version.models.Post
import coil3.compose.AsyncImage

@Composable
fun FeedPostCard(
    post: Post,
    modifier: Modifier = Modifier
) {
    val primaryOrange = Color(0xFFFF8C00)        // Orange for interactions
    val backgroundWhite = Color(0xFFFFFFFF)      // White background
    val blackText = Color(0xFF333333)            // Black username
    val grayText = Color(0xFF666666)             // Gray caption
    val borderGray = Color(0xFFF0F0F0)           // Subtle border

    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(42) } // Mock like count

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundWhite
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 👤 USER HEADER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Profile Picture Placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(borderGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.userName.take(1).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryOrange
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    // BLACK USERNAME
                    Text(
                        text = post.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = blackText
                    )
                    Text(
                        text = "1 hrs ago",
                        fontSize = 12.sp,
                        color = grayText
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // More options (three dots)
                IconButton(
                    onClick = { /* Options menu */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text(
                        "⋮",
                        fontSize = 16.sp,
                        color = grayText
                    )
                }
            }

            // 📷 POST IMAGE
            AsyncImage(
                model = post.photoUrl,
                contentDescription = "User's uploaded photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            //  CAPTION (if exists)
            if (post.caption.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = post.caption,
                    fontSize = 14.sp,
                    color = grayText,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ❤️ LIKE & COMMENT ICONS ROW
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isLiked = !isLiked
                            likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else grayText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "$likeCount",
                        fontSize = 14.sp,
                        color = grayText,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { /* Comment action */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = grayText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "12",
                        fontSize = 14.sp,
                        color = grayText,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Share Button (optional)
                IconButton(
                    onClick = { /* Share action */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = grayText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}