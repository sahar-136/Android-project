package com.example.version.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.version.viewmodel.UploadViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.version.util.Resource
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(navController: NavController) {
    val uploadViewModel: UploadViewModel = hiltViewModel()
    val uploadState by uploadViewModel.uploadState.observeAsState()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf(TextFieldValue()) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // ⭐ Deep Royal Purple Background - Same as Login/Register
    val royalDark = Color(0xFF14082B)
    val royalDeep = Color(0xFF2A0E4A)
    val royalRich = Color(0xFF3E1C6D)

    // ⭐ Soft Shine Glow (subtle)
    val royalGlow = Color(0xFF6A35B8).copy(alpha = 0.25f)

    // ⭐ Deep Peach Colors
    val deepPeachStart = Color(0xFFE8765C)
    val deepPeachEnd = Color(0xFFD45C47)

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Camera launcher that captures image to URI
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempImageUri
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Deep Royal Purple Background
                Brush.verticalGradient(
                    colors = listOf(
                        royalRich,
                        royalDeep,
                        royalDark
                    )
                )
            )
    ) {
        // ✨ Deep Lines & Bubbles - Radial Gradient with Overlapping Effects
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            royalGlow,
                            Color.Transparent,
                            Color(0xFF4A2C85).copy(alpha = 0.15f) // Deep lines effect
                        ),
                        radius = 1200f
                    )
                )
        )

        // Royal Glow Effect - Subtle Background Shine
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF5B2C87).copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("New Post", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent // Transparent to show background
                    )
                )
            },
            containerColor = Color.Transparent // Transparent to show background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // FIXED: Image preview area with centered text
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(BorderStroke(3.dp, deepPeachStart), RoundedCornerShape(16.dp))
                    )
                } else {
                    // Placeholder when no image selected - FIXED CENTERING
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(BorderStroke(3.dp, deepPeachStart), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp) // Added padding for better layout
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = deepPeachStart,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "Add Your Photo",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Black,
                                textAlign = TextAlign.Center // Center alignment
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Choose from gallery or take a new photo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center, // FIXED: Center alignment
                                modifier = Modifier.fillMaxWidth() // FIXED: Full width for proper centering
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Deep Peach Gradient Buttons - Equal size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Gallery button with gradient
                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(deepPeachStart, deepPeachEnd)
                                ),
                                shape = RoundedCornerShape(25.dp)
                            )
                    ) {
                        Icon(
                            Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery", color = Color.White)
                    }

                    // Camera button with gradient
                    Button(
                        onClick = {
                            val uri = createImageUri(context)
                            tempImageUri = uri
                            cameraLauncher.launch(uri)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(deepPeachStart, deepPeachEnd)
                                ),
                                shape = RoundedCornerShape(25.dp)
                            )
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Camera", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Optional Caption section - White field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Add Caption (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = caption,
                        onValueChange = { newValue ->
                            if (newValue.text.length <= 500) {
                                caption = newValue
                            }
                        },
                        placeholder = {
                            Text(
                                "Share what makes this photo special... (Optional)",
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.95f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = deepPeachStart
                        )
                    )

                    Text(
                        "${caption.text.length}/500",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Deep Peach Upload Button - Always Peach Gradient
                Button(
                    onClick = {
                        if (imageUri != null) {
                            uploadViewModel.uploadPhoto(imageUri!!, caption.text)
                        }
                    },
                    enabled = imageUri != null,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            // Always peach gradient, regardless of enabled state
                            Brush.horizontalGradient(
                                colors = if (imageUri != null) {
                                    listOf(deepPeachStart, deepPeachEnd)
                                } else {
                                    listOf(
                                        deepPeachStart.copy(alpha = 0.5f),
                                        deepPeachEnd.copy(alpha = 0.5f)
                                    )
                                }
                            ),
                            shape = RoundedCornerShape(30.dp)
                        )
                ) {
                    Text(
                        "UPLOAD PHOTO",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Status feedback
                when (uploadState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Uploading...", color = Color.White)
                    }
                    is Resource.Error -> {
                        Text(
                            (uploadState as Resource.Error).message ?: "Upload failed",
                            color = Color.Red
                        )
                    }
                    is Resource.Success -> {
                        Text(
                            "✓ Photo uploaded successfully!",
                            color = Color(0xFF4CAF50)
                        )
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(1500)
                            navController.popBackStack()
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

// Helper function to create image URI for camera capture
fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "IMG_${timeStamp}.jpg"
    )
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}