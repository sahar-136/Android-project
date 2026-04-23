package com.example.version.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.version.models.User
import com.example.version.navigation.Routes
import com.example.version.ui.theme.AppColors
import com.example.version.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    val query by searchViewModel.query.collectAsState()
    val userList by searchViewModel.users.collectAsState()
    val loading by searchViewModel.loading.collectAsState()

    val trimmed = query.trim()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Search Users",
                        color = AppColors.BlackText,
                        fontWeight = FontWeight.Bold
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
        },
        containerColor = AppColors.BackgroundWhite
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundWhite)
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { searchViewModel.onQueryChange(it) },
                placeholder = { Text(text = "Search by name or username...", color = AppColors.TextGray) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                trailingIcon = {
                    if (trimmed.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchViewModel.onQueryChange("")
                                focusManager.clearFocus()
                            }
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = AppColors.TextGray
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AppColors.LightGray,
                    focusedBorderColor = AppColors.PrimaryOrange,
                    unfocusedBorderColor = AppColors.BorderGray
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    color = AppColors.BlackText
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.PrimaryOrange)
                    }
                }

                trimmed.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            "Type a name or username to search.",
                            color = AppColors.TextGray
                        )
                    }
                }

                userList.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No users found", color = AppColors.TextGray)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(userList) { user ->
                            SearchUserCard(
                                user = user,
                                onCardClick = {
                                    focusManager.clearFocus()

                                    // Safety: avoid navigating with blank userId
                                    val uid = user.userId.trim()
                                    if (uid.isNotEmpty()) {
                                        navController.navigate(Routes.profile(uid)) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .border(BorderStroke(2.dp, AppColors.PrimaryOrange), CircleShape)
                    .clip(CircleShape)
                    .background(AppColors.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (user.profileImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "${user.name} profile image",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        user.name.take(1).ifBlank { "?" }.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = AppColors.PrimaryOrange
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.name,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.BlackText,
                    fontSize = 17.sp
                )
                Text("@${user.username}", fontSize = 14.sp, color = AppColors.TextGray)

                if (user.bio.isNotBlank()) {
                    Text(
                        user.bio,
                        fontSize = 13.sp,
                        color = AppColors.DarkGray,
                        maxLines = 1
                    )
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