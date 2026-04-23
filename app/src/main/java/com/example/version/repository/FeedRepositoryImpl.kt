package com.example.version.repository

import android.util.Log
import com.example.version.models.Post
import com.example.version.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
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
            .whereEqualTo("deleted", false)
            .orderBy("uploadTimestamp", Query.Direction.DESCENDING)
            .limit(50)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FIREBASE_FULL", error.toString())
                Log.e("FIREBASE_FULL", Log.getStackTraceString(error))
                Log.e("FIREBASE_FULL", error.message ?: "No message")

                trySend(Resource.Error(error.message ?: "Something went wrong"))
                return@addSnapshotListener
            }

            // ✅ FIX: Handle null uploadTimestamp
            val posts = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val post = doc.toObject(Post::class.java)
                    if (post != null) {
                        post.copy(
                            id = doc.id,
                            uploadTimestamp = post.uploadTimestamp ?: Timestamp.now()
                        )
                    } else {
                        Post(id = doc.id, uploadTimestamp = Timestamp.now())
                    }
                } catch (e: Exception) {
                    Log.e("FeedRepo", "Error parsing post: ${e.message}")
                    null
                }
            }.orEmpty()

            Log.d("FeedRepo", "Loaded ${posts.size} posts from feed")
            trySend(Resource.Success(posts))
        }

        awaitClose { listener.remove() }
    }

    override fun getTrendingPosts(): Flow<Resource<List<Post>>> = callbackFlow {

        trySend(Resource.Loading)

        val query = firestore.collection("posts")
            .whereEqualTo("deleted", false)
            .orderBy("likesCount", Query.Direction.DESCENDING)
            .limit(50)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FIREBASE_FULL", error.toString())
                Log.e("FIREBASE_FULL", Log.getStackTraceString(error))
                Log.e("FIREBASE_FULL", error.message ?: "No message")

                trySend(Resource.Error(error.message ?: "Something went wrong"))
                return@addSnapshotListener
            }

            // ✅ FIX: Handle null uploadTimestamp
            val posts = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val post = doc.toObject(Post::class.java)
                    if (post != null) {
                        post.copy(
                            id = doc.id,
                            uploadTimestamp = post.uploadTimestamp ?: Timestamp.now()
                        )
                    } else {
                        Post(id = doc.id, uploadTimestamp = Timestamp.now())
                    }
                } catch (e: Exception) {
                    Log.e("FeedRepo", "Error parsing trending post: ${e.message}")
                    null
                }
            }.orEmpty()

            Log.d("FeedRepo", "Loaded ${posts.size} trending posts")
            trySend(Resource.Success(posts))
        }

        awaitClose { listener.remove() }
    }

    override suspend fun getPostCommentsCount(postId: String): Flow<Int> = callbackFlow {
        val listener = firestore.collection("posts")
            .document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d("comment", "$postId :$error")
                    trySend(0)
                    return@addSnapshotListener
                }

                val count = snapshot?.getLong("commentsCount")?.toInt() ?: 0
                trySend(count)
            }

        awaitClose { listener.remove() }
    }
}