package com.example.version.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
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
import androidx.core.content.ContextCompat
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
    val profileImageState by editProfileViewModel.profileImageState.collectAsState()
    val context = LocalContext.current

    // For image picker
    var showPickerDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showImageUploadError by remember { mutableStateOf<String?>(null) }

    // TextField states
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }

    // ✅ Track if image was selected
    var imageSelected by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // When loaded, pre-fill fields
    LaunchedEffect(userState) {
        if (userState is Resource.Success) {
            val user = (userState as Resource.Success<com.example.version.models.User>).data
            Log.d("EditProfileScreen", "User loaded: name=${user.name}, profileImageUrl=${user.profileImageUrl}")
            name = user.name
            username = user.username
            bio = user.bio
            profileImageUrl = user.profileImageUrl
        }
    }

    // ✅ OBSERVE PROFILE IMAGE UPLOAD STATE
    LaunchedEffect(profileImageState) {
        when (profileImageState) {
            is Resource.Success -> {
                val downloadUrl = (profileImageState as Resource.Success<String>).data
                Log.d("EditProfileScreen", "Image uploaded successfully: $downloadUrl")
                profileImageUrl = downloadUrl
                showImageUploadError = null
            }
            is Resource.Error -> {
                val error = (profileImageState as Resource.Error).message
                Log.e("EditProfileScreen", "Image upload failed: $error")
                showImageUploadError = error
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        editProfileViewModel.loadUserProfile()
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("EditProfileScreen", "Camera permission granted")
        } else {
            Log.e("EditProfileScreen", "Camera permission denied")
            showImageUploadError = "Camera permission required"
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("EditProfileScreen", "Gallery image selected: $it")
            imageSelected = true
            selectedImageUri = it
            profileImageUrl = it.toString()
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        Log.d("EditProfileScreen", "Camera result: success=$success, tempUri=$tempCameraUri")

        if (success && tempCameraUri != null) {
            Log.d("EditProfileScreen", "Photo captured successfully")
            imageSelected = true
            selectedImageUri = tempCameraUri
            profileImageUrl = tempCameraUri.toString()
        } else {
            Log.e("EditProfileScreen", "Camera capture failed")
            showImageUploadError = "Failed to capture photo"
        }
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
                        if (profileImageUrl.isNotBlank() && !profileImageUrl.startsWith("file://")) {
                            // Remote image from Firestore
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.LightGray)
                            )
                        } else if (profileImageUrl.isNotBlank()) {
                            // Local file preview
                            AsyncImage(
                                model = Uri.parse(profileImageUrl),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.LightGray)
                            )
                        } else {
                            // Fallback to initial
                            Text(
                                text = name.take(1).uppercase(),
                                fontSize = 38.sp,
                                color = AppColors.PrimaryOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.LightGray),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        // Show loading during upload
                        if (profileImageState is Resource.Loading || editResult is Resource.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(34.dp)
                                    .align(Alignment.BottomEnd),
                                color = AppColors.PrimaryOrange,
                                strokeWidth = 2.dp
                            )
                        } else {
                            FloatingActionButton(
                                onClick = { showPickerDialog = true },
                                shape = CircleShape,
                                containerColor = AppColors.PrimaryOrange,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Change Photo")
                            }
                        }
                    }

                    // Show image upload error
                    if (showImageUploadError != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "⚠️ ${showImageUploadError}",
                            color = AppColors.ErrorRed,
                            fontSize = 12.sp
                        )
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

                                    val hasCameraPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasCameraPermission) {
                                        val photoFile = createImageFile(context)
                                        Log.d("EditProfileScreen", "photoFile path: ${photoFile.absolutePath}")
                                        val cameraUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            photoFile
                                        )
                                        tempCameraUri = cameraUri
                                        cameraLauncher.launch(cameraUri)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
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

                    // ✅ FIXED: Save Changes button
                    Button(
                        onClick = {
                            val user = (userState as Resource.Success<com.example.version.models.User>).data

                            // ✅ If image selected, use combined function (sequential execution)
                            if (imageSelected && selectedImageUri != null) {
                                Log.d("EditProfileScreen", "Image selected - uploading first, then updating profile")
                                editProfileViewModel.updateProfileImageAndUserProfile(
                                    userId = user.userId,
                                    imageUri = selectedImageUri!!,
                                    name = name,
                                    username = username,
                                    bio = bio
                                )
                                imageSelected = false
                            } else {
                                // ✅ No image change - just update profile info
                                Log.d("EditProfileScreen", "No image change - updating profile only")
                                editProfileViewModel.updateUserProfileOnly(user.userId, name, username, bio)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryOrange),
                        // ✅ Disable while updating
                        enabled = editResult !is Resource.Loading && profileImageState !is Resource.Loading
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Show edit result
                    when (editResult) {
                        is Resource.Loading -> {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = AppColors.PrimaryOrange, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Updating...", color = AppColors.TextGray)
                            }
                        }
                        is Resource.Success -> {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("✅ Profile updated!", color = AppColors.PrimaryOrange, fontWeight = FontWeight.Bold)

                            // ✅ Wait 2 seconds before navigation
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(2000)
                                navController.popBackStack()
                            }
                        }
                        is Resource.Error -> {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "❌ ${(editResult as Resource.Error).message ?: "Update failed"}",
                                color = AppColors.ErrorRed,
                                fontSize = 12.sp
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val picturesDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val storageDir: File = picturesDir ?: context.cacheDir
    val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    Log.d("EditProfileScreen", "createImageFile -> ${file.absolutePath}")
    return file
}
