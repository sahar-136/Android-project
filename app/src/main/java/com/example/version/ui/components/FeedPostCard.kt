package com.example.version.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.version.models.Post
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun FeedPostCard(post: Post) {

    // ⭐ Deep Peach Color for Username (Same as Login/Register buttons)
    val deepPeachColor = Color(0xFFE8765C)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // White background maintained
        elevation = CardDefaults.cardElevation(4.dp) // Slightly more elevation for better look
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = post.photoUrl,
                contentDescription = "User's uploaded photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop // so image fits nicely
            )
            Spacer(modifier = Modifier.height(12.dp))

            // USER NAME - Deep Peach Color
            Text(
                text = post.userName,
                style = MaterialTheme.typography.titleMedium,
                color = deepPeachColor // Deep peach color applied
            )
            Spacer(modifier = Modifier.height(6.dp))

            // CAPTION - Black text for readability
            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
    }
}