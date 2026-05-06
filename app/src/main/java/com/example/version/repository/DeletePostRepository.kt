package com.example.version.repository

import com.example.version.util.Resource

interface DeletePostRepository {
    suspend fun deletePost(postId: String, userId: String, photoUrl: String): Resource<Boolean>
}