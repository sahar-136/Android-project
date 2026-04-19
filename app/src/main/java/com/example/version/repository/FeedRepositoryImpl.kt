package com.example.version.repository

import android.util.Log
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

    // ============================================
    // GET ALL FEED POSTS (Real-time)
    // ============================================
    override fun getFeedPosts(): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading)

        val query = firestore.collection("posts")
            .orderBy("uploadTimestamp", Query.Direction.DESCENDING)
            .limit(50)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Something went wrong"))
                return@addSnapshotListener
            }

            val posts = snapshot?.toObjects(Post::class.java)
                ?.filter { post ->
                    post.isDeleted == false
                } ?: emptyList()

            trySend(Resource.Success(posts))
        }

        awaitClose { listener.remove() }
    }

    // ============================================
    // GET COMMENTS COUNT (Real-time)
    // ============================================
    override suspend fun getPostCommentsCount(postId: String): Flow<Int> = callbackFlow {
        val listener = firestore
            .collection("posts")
            .document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d("comment", "$postId :$error")
                    trySend(0)
                    return@addSnapshotListener
                }

                val commentsCount = snapshot?.getLong("commentsCount")?.toInt() ?: 0
                Log.d("comment", "$postId :$commentsCount")

                trySend(commentsCount)
            }

        awaitClose { listener.remove() }
    }
}