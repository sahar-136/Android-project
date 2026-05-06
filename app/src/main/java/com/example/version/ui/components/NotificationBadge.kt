package com.example.version.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.version.ui.theme.AppColors

@Composable
fun NotificationBell(
    unreadCount: Int,
    onClick: () -> Unit
) {
    Box {
        // ✅ BELL ICON
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notifications",
                tint = AppColors.BlackText
            )
        }

        // ✅ BLACK DOT (Only if unread > 0)
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = AppColors.BlackText,
                        shape = CircleShape
                    )
                    .align(Alignment.TopEnd)
            )
        }
    }
}