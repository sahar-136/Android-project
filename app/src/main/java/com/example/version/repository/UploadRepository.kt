package com.example.version.repository

import android.net.Uri
import com.example.version.models.Post
import com.example.version.util.Resource

interface UploadRepository {
    suspend fun uploadPhotoAndCreatePost(
        userId: String,
        fileUri: Uri,
        caption: String? = null
    ): Resource<Post>
}