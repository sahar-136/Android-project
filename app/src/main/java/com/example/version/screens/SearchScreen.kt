package com.example.version.screens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.version.models.User
import com.example.version.ui.theme.AppColors
import com.example.version.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: androidx.navigation.NavController,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    var query by remember { mutableStateOf("") }

    val userList by searchViewModel.users.collectAsState()
    val loading by searchViewModel.loading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Users", color = AppColors.BlackText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.BlackText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.PrimaryOrange)
            )
        },
        containerColor = AppColors.BackgroundWhite
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundWhite)
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    searchViewModel.search(query.trim())
                },
                placeholder = { Text(text = "Search users...", color = AppColors.TextGray) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AppColors.LightGray,
                    focusedBorderColor = AppColors.PrimaryOrange,
                    unfocusedBorderColor = AppColors.BorderGray,
                    focusedLabelColor = AppColors.PrimaryOrange
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = AppColors.BlackText)
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.PrimaryOrange)
                }
            } else if (userList.isEmpty() && query.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No users found", color = AppColors.TextGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    items(userList) { user ->
                        SearchUserCard(
                            user = user,
                            onCardClick = {
                                // Navigate to user's profile:
                                navController.navigate("profile/${user.userId}")
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SearchUserCard(
    user: User,
    onCardClick: () -> Unit
) {
    Surface(
        onClick = onCardClick,
        shape = RoundedCornerShape(18.dp),
        color = AppColors.BackgroundWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(2.dp, AppColors.PrimaryOrange),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 14.dp, horizontal = 12.dp)
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .border(BorderStroke(2.dp, AppColors.PrimaryOrange), CircleShape)
                    .clip(CircleShape)
                    .background(AppColors.LightGray)
            ) {
                if (!user.profileImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "${user.name} profile image",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback: First letter of name
                    Text(
                        user.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = AppColors.PrimaryOrange,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(user.name, fontWeight = FontWeight.Bold, color = AppColors.BlackText, fontSize = 17.sp)
                Text("@${user.username}", fontSize = 14.sp, color = AppColors.TextGray)
                user.bio?.let {
                    if (it.isNotBlank()) {
                        Text(it, fontSize = 13.sp, color = AppColors.DarkGray, maxLines = 1)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "View profile",
                tint = AppColors.PrimaryOrange
            )
        }
    }
}