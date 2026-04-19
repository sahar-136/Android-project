package com.example.version.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.version.models.Comment
import com.example.version.ui.theme.AppColors
import com.example.version.util.Resource
import com.example.version.viewmodel.CommentViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    postId: String,
    navController: NavController,
    commentViewModel: CommentViewModel = hiltViewModel()
){
    if (postId.isEmpty()) {
        Text("Error: Post ID missing")
        return
    }

    val commentsState by commentViewModel.comments.collectAsState()
    val addCommentState by commentViewModel.addCommentState.collectAsState()
    val deleteCommentState by commentViewModel.deleteCommentState.collectAsState()
    val commentLikeCounts by commentViewModel.commentLikeCounts.collectAsState()
    val commentLikeStatus by commentViewModel.commentLikeStatus.collectAsState()

    var commentText by remember { mutableStateOf("") }
    var selectedCommentForDelete by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        commentViewModel.loadCommentsForPost(postId)
    }
    DisposableEffect(Unit) {
        onDispose {
            commentViewModel.resetStates()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundWhite)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Comments",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.BlackText
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.BlackText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppColors.PrimaryOrange,
                titleContentColor = AppColors.BlackText
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppColors.BackgroundWhite)
        ) {
            when (commentsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.PrimaryOrange,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                is Resource.Error -> {
                    val errorMsg = (commentsState as Resource.Error).message ?: "Unknown error"
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                                "Unable to load comments",
                                color = AppColors.BlackText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                errorMsg,
                                color = AppColors.TextGray,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                is Resource.Success<*> -> {
                    val comments = (commentsState as? Resource.Success<List<Comment>>)?.data
                        ?: emptyList()
                    if (comments.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    Icons.Filled.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = AppColors.PrimaryOrange,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No comments yet",
                                    color = AppColors.BlackText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Be the first to comment!",
                                    color = AppColors.TextGray,
                                    fontSize = 14.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 12.dp,
                                bottom = 12.dp,
                                start = 12.dp,
                                end = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(comments) { comment ->
                                CommentItemCard(
                                    comment = comment,
                                    likeCount = commentViewModel.getCommentLikeCount(comment.commentId),
                                    isLiked = commentViewModel.isCommentLiked(comment.commentId),
                                    onLikeClick = {
                                        commentViewModel.toggleCommentLike(postId, comment.commentId)
                                    },
                                    onDeleteClick = {
                                        selectedCommentForDelete = comment.commentId
                                        showDeleteDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
        // ======= MODERN FLOATING COMMENT INPUT WITH ALWAYS ORANGE BUTTON =======
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.LightGray)
                .padding(start = 8.dp, end = 8.dp, bottom = 12.dp, top = 0.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { newValue ->
                            if (newValue.length <= 250) commentText = newValue
                        },
                        placeholder = {
                            Text(
                                "Add a comment...",
                                color = AppColors.TextGray,
                                fontSize = 15.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = AppColors.PrimaryOrange,
                            unfocusedBorderColor = AppColors.BorderGray,
                            focusedTextColor = AppColors.BlackText,
                            unfocusedTextColor = AppColors.BlackText,
                            cursorColor = AppColors.PrimaryOrange
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = false
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                commentViewModel.addComment(postId, commentText)
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank() && addCommentState !is Resource.Loading,
                        modifier = Modifier
                            .size(44.dp)
                            .background(AppColors.PrimaryOrange, shape = androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send comment",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            // Progress indicator inside field
            if (addCommentState is Resource.Loading) {
                CircularProgressIndicator(
                    color = AppColors.PrimaryOrange,
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 30.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Comment",
                    color = AppColors.BlackText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this comment?",
                    color = AppColors.TextGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedCommentForDelete != null) {
                            commentViewModel.deleteComment(postId, selectedCommentForDelete!!)
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.ErrorRed
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Delete",
                        color = AppColors.ButtonTextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        "Cancel",
                        color = AppColors.PrimaryOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = AppColors.BackgroundWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CommentItemCard(
    comment: Comment,
    likeCount: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ==== Orange border all 4 sides ====
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = AppColors.PrimaryOrange,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.LightGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        comment.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.BlackText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        comment.getTimeAgo(),
                        fontSize = 12.sp,
                        color = AppColors.TextGray
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Delete comment",
                        tint = AppColors.TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                comment.commentText,
                fontSize = 14.sp,
                color = AppColors.BlackText,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like comment",
                        tint = if (isLiked) AppColors.ErrorRed else AppColors.TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    "$likeCount likes",
                    fontSize = 12.sp,
                    color = AppColors.TextGray
                )
            }
        }
    }
}