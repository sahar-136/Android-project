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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // PHOTO (sab se pehle)
            AsyncImage(
                model = post.photoUrl,
                contentDescription = "User's uploaded photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop // so image fits nicely
            )
            Spacer(modifier = Modifier.height(8.dp))
            // USER NAME
            val LinkedInBlue = Color(0xFF0A66C2)
            Text(
                text = post.userName,
                style = MaterialTheme.typography.titleMedium,
                color = LinkedInBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            // CAPTION
            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}