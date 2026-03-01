package com.example.version.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.version.viewmodel.UploadViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.version.util.Resource
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val SnapBlue = Color(0xFF0A66C2)
private val SnapGray = Color(0xFFF2F2F2)
private val SnapButtonGray = Color(0xFFBDBDBD)
private val SnapTextGray = Color(0xFF616161)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(navController: NavController) {
    val uploadViewModel: UploadViewModel = hiltViewModel()
    val uploadState by uploadViewModel.uploadState.observeAsState()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf(TextFieldValue()) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var useFrontCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Photo (camera) launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) imageUri = tempImageUri
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Post", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back to Feed", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SnapGray)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            // Image preview
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(2.dp, SnapBlue), RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    Modifier
                        .size(200.dp)
                        .border(BorderStroke(2.dp, SnapBlue), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = SnapBlue,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Add Your Daily Photo", style = MaterialTheme.typography.titleLarge, color = Color.Black)
                        Text("Choose from gallery or take a photo", style = MaterialTheme.typography.bodySmall, color = SnapTextGray)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Front/Back toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { useFrontCamera = false },
                    colors = ButtonDefaults.buttonColors(if (!useFrontCamera) SnapBlue else SnapButtonGray)
                ) { Text("Back Camera") }
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = { useFrontCamera = true },
                    colors = ButtonDefaults.buttonColors(if (useFrontCamera) SnapBlue else SnapButtonGray)
                ) { Text("Front Camera") }
            }

            // Gallery & Camera buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = SnapBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Choose Gallery")
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        // Create URI for camera photo
                        val uri = createImageUri(context)
                        tempImageUri = uri
                        // Compose's official contracts don't support front/back directly,
                        // but passing intent extras may work on some devices:
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                        if (useFrontCamera) {
                            intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
                            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
                        } else {
                            intent.putExtra("android.intent.extras.CAMERA_FACING", 0)
                            intent.putExtra("android.intent.extras.LENS_FACING_BACK", 1)
                        }
                        cameraLauncher.launch(uri)
                    },
                    enabled = true, // Enable camera always
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SnapBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Take a Photo")
                }
            }
            Spacer(Modifier.height(20.dp))

            // Caption Field
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Add Caption", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                OutlinedTextField(
                    value = caption,
                    onValueChange = { newValue ->
                        if (newValue.text.length <= 500) caption = newValue
                    },
                    placeholder = { Text("Share what makes this photo special…", color = SnapTextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = SnapBlue,
                        unfocusedBorderColor = SnapBlue,
                        focusedLabelColor = SnapBlue,
                        unfocusedLabelColor = SnapBlue,
                        cursorColor = SnapBlue
                    )
                )
                Text("${caption.text.length}/500", style = MaterialTheme.typography.bodySmall, color = SnapTextGray, modifier = Modifier.align(Alignment.End))
            }
            Spacer(Modifier.height(24.dp))

            // Upload Button (caption ab optional hai!)
            Button(
                onClick = {
                    if (imageUri != null) {
                        uploadViewModel.uploadPhoto(imageUri!!, caption.text)
                    }
                },
                enabled = imageUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SnapBlue, contentColor = Color.White),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text("UPLOAD PHOTO")
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Show progress/error/success
            when (uploadState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = SnapBlue
                    )
                }
                is Resource.Error -> {
                    Text(
                        (uploadState as Resource.Error).message ?: "Error uploading photo",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                is Resource.Success -> {
                    Text(
                        "Uploaded!",
                        color = Color(0xFF388E3C),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
                else -> {}
            }
        }
    }
}

/**
 * Helper function to create a URI for the photo to be stored in external cache dir.
 */
fun createImageUri(context: Context): Uri {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "JPEG_${timeStamp}_.jpg")
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        context.packageName + ".provider", // Make sure to define provider in Manifest!
        file
    )
}