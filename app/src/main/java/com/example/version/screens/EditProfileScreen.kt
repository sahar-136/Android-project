package com.example.version.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.version.viewmodel.EditProfileViewModel
import com.example.version.util.Resource
import com.example.version.ui.theme.AppColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: androidx.navigation.NavController,
    editProfileViewModel: EditProfileViewModel = hiltViewModel()
) {
    val userState by editProfileViewModel.userState.collectAsState()
    val editResult by editProfileViewModel.editResult.collectAsState()
    val context = LocalContext.current

    // For image picker
    var showPickerDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // TextField states
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }

    // When loaded, pre-fill fields
    LaunchedEffect(userState) {
        if (userState is Resource.Success) {
            val user = (userState as Resource.Success<com.example.version.models.User>).data
            name = user.name
            username = user.username
            bio = user.bio
            profileImageUrl = user.profileImageUrl
        }
    }

    LaunchedEffect(Unit) {
        editProfileViewModel.loadUserProfile()
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val user = (userState as? Resource.Success<com.example.version.models.User>)?.data
            if (user != null) {
                editProfileViewModel.updateProfileImage(user.userId, it)
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val user = (userState as? Resource.Success<com.example.version.models.User>)?.data
        if (success && tempCameraUri != null && user != null) {
            editProfileViewModel.updateProfileImage(user.userId, tempCameraUri!!)
        }
        // Clear tempCameraUri after use
        tempCameraUri = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Profile", fontWeight = FontWeight.Bold, color = AppColors.BlackText)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.BlackText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.PrimaryOrange)
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundWhite)
                .padding(paddingValues)
        ) {
            if (userState is Resource.Loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = AppColors.PrimaryOrange)
            } else if (userState is Resource.Error) {
                Text(
                    text = (userState as Resource.Error).message ?: "Failed to load user",
                    color = AppColors.ErrorRed,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (userState is Resource.Success) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture with change button
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (profileImageUrl.isNotBlank()) {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.LightGray)
                            )
                        } else {
                            Text(
                                text = name.take(1).uppercase(),
                                fontSize = 38.sp,
                                color = AppColors.PrimaryOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.LightGray)
                            )
                        }
                        FloatingActionButton(
                            onClick = { showPickerDialog = true },
                            shape = CircleShape,
                            containerColor = AppColors.PrimaryOrange,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Change Photo")
                        }
                    }

                    // Picker dialog
                    if (showPickerDialog) {
                        AlertDialog(
                            onDismissRequest = { showPickerDialog = false },
                            title = { Text("Change Profile Photo") },
                            text = { Text("Choose an option") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showPickerDialog = false
                                    // Camera
                                    val photoFile = createImageFile(context)
                                    val cameraUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        photoFile
                                    )
                                    tempCameraUri = cameraUri
                                    cameraLauncher.launch(cameraUri)
                                }) {
                                    Text("Take Photo")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showPickerDialog = false
                                    galleryLauncher.launch("image/*")
                                }) {
                                    Text("Choose from Gallery")
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 25) name = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { if (it.length <= 25) username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))

                    // Bio
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { if (it.length <= 150) bio = it },
                        label = { Text("Bio") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
                    )
                    Spacer(Modifier.height(18.dp))

                    // Save Changes button
                    Button(
                        onClick = {
                            val user = (userState as Resource.Success<com.example.version.models.User>).data
                            editProfileViewModel.updateUserProfile(user.userId, name, username, bio)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryOrange)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Show edit result (success/failure)
                    when (editResult) {
                        is Resource.Loading -> {
                            Spacer(modifier = Modifier.height(14.dp))
                            CircularProgressIndicator(color = AppColors.PrimaryOrange)
                        }
                        is Resource.Success -> {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Profile updated!", color = AppColors.PrimaryOrange)
                        }
                        is Resource.Error -> {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text((editResult as Resource.Error).message
                                ?: "Update failed",
                                color = AppColors.ErrorRed)
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

// Helper function to create a temp file for the camera image
fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File = context.cacheDir
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}