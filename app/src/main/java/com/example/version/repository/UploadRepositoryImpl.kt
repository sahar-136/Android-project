package com.example.version.repository

import android.net.Uri
import com.example.version.models.Post
import com.example.version.util.Resource
import javax.inject.Inject

class UploadRepositoryImpl @Inject constructor() : UploadRepository {
    override suspend fun uploadPhotoAndCreatePost(
        userId: String,
        fileUri: Uri,
        caption: String?
    ): Resource<Post> {
        // TODO: Implement Firebase Storage upload and Firestore post creation logic here
        return Resource.Error("Not implemented")
    }
}