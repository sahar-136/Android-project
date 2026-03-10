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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.version.viewmodel.UploadViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.version.util.Resource
import com.example.version.ui.theme.AppColors
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

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempImageUri
        }
    }

    // CLEAN LAYOUT WITH PROPER SPACING
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundWhite) //WHITE SCREEN BACKGROUND
    ) {
        // DARK ORANGE TOP BAR - BLACK ARROW & TEXT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.PrimaryOrange) // DARK ORANGE TOP BAR
                .statusBarsPadding() // Handle status bar
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(40.dp) // Proper touch target
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.BlackText, // BLACK BACK ARROW
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp)) // Reduced spacing
                Text(
                    "New Post",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.BlackText // BLACK TEXT
                )
            }
        }

        // SCROLLABLE CONTENT - PROPER SPACING
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp), // Consistent padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 📷 IMAGE PREVIEW AREA
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .size(260.dp) // Slightly smaller for better spacing
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            color = AppColors.LightGray,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            } else {
                // Placeholder
                Box(
                    modifier = Modifier
                        .size(260.dp) // Consistent with image size
                        .background(
                            color = AppColors.LightGray,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = AppColors.BorderGray,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = AppColors.TextGray,
                            modifier = Modifier.size(64.dp) // Smaller icon
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Choose or take a photo",
                            fontSize = 14.sp, // Smaller text
                            color = AppColors.TextGray,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) //REDUCED SPACING

            //GALLERY & CAMERA BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gallery button
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PrimaryOrange
                    ),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(
                        Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        tint = AppColors.ButtonTextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Gallery",
                        color = AppColors.ButtonTextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Camera button
                Button(
                    onClick = {
                        val uri = createImageUri(context)
                        tempImageUri = uri
                        cameraLauncher.launch(uri)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PrimaryOrange
                    ),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = AppColors.ButtonTextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Camera",
                        color = AppColors.ButtonTextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // PROPER SPACING TO CAPTION

            //CAPTION SECTION - CENTER POSITION
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Add Caption (Optional)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.BlackText
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = caption,
                    onValueChange = { newValue ->
                        if (newValue.text.length <= 200) {
                            caption = newValue
                        }
                    },
                    placeholder = {
                        Text(
                            "Write something about your photo...",
                            color = AppColors.TextGray,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.LightGray,
                        unfocusedContainerColor = AppColors.LightGray,
                        focusedBorderColor = AppColors.PrimaryOrange,
                        unfocusedBorderColor = AppColors.BorderGray,
                        focusedTextColor = AppColors.BlackText,
                        unfocusedTextColor = AppColors.BlackText,
                        cursorColor = AppColors.PrimaryOrange
                    )
                )

                // Character counter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "${caption.text.length}/200",
                        fontSize = 12.sp,
                        color = AppColors.TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // ✅ PROPER SPACING TO UPLOAD BUTTON

            //UPLOAD BUTTON - PROPER POSITION
            Button(
                onClick = {
                    if (imageUri != null) {
                        uploadViewModel.uploadPhoto(imageUri!!, caption.text)
                    }
                },
                enabled = imageUri != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryOrange,           // Same dark orange when enabled
                    disabledContainerColor = AppColors.PrimaryOrange    // Same dark orange when disabled
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    "UPLOAD PHOTO",
                    color = AppColors.ButtonTextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp)) // SPACING FOR STATUS

            // STATUS FEEDBACK - COMPACT
            when (uploadState) {
                is Resource.Loading -> {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.PrimaryOrange,
                            modifier = Modifier.size(24.dp) // Smaller loader
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Uploading your photo...",
                            color = AppColors.TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
                is Resource.Error -> {
                    Text(
                        (uploadState as Resource.Error).message ?: "Upload failed. Please try again.",
                        color = AppColors.ErrorRed,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
                is Resource.Success -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "✓ Photo uploaded successfully!",
                            color = AppColors.SuccessGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Returning to feed...",
                            color = AppColors.TextGray,
                            fontSize = 13.sp
                        )
                    }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        navController.popBackStack()
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(20.dp)) //BOTTOM PADDING
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