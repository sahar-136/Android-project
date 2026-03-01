package com.example.version.repository

import com.example.version.models.Post
import com.example.version.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FeedRepository {

    override fun getFeedPosts(): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading)

        val query = firestore.collection("posts")
            .whereEqualTo("isDeleted", false) // Only show non-deleted posts
            .orderBy("uploadTimestamp", Query.Direction.DESCENDING)
            .limit(30) // First batch: 30 recent posts

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Something went wrong"))
                return@addSnapshotListener
            }

            val posts = snapshot?.toObjects(Post::class.java) ?: emptyList()
            trySend(Resource.Success(posts))
        }
        awaitClose { listener.remove() }
    }
}