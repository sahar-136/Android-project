package com.example.version

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.version.navigation.AuthNavGraph
import com.example.version.screens.MainScaffold
import com.example.version.ui.theme.VersionTheme
import com.example.version.viewmodel.AuthViewModel
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var mainNavController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity_INIT", "🚀 Activity created")

        // ✅ Notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        // ✅ FCM topic subscribe
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnSuccessListener {
                Log.d("FCM", "✅ Subscribed to all_users")
            }
            .addOnFailureListener {
                Log.e("FCM", "❌ Subscribe failed: ${it.message}")
            }

        setContent {
            VersionTheme {

                val rootNavController = rememberNavController()
                val mainNavController = rememberNavController()
                this@MainActivity.mainNavController = mainNavController

                // ✅ Single Activity-scoped AuthViewModel — the ONLY instance observed for navigation
                val authViewModel: AuthViewModel = hiltViewModel()
                val isLoggedIn by authViewModel.isLoggedIn.observeAsState(false)

                Log.d("MainActivity_RECOMPOSE", "🔄 Recomposing... isLoggedIn: $isLoggedIn")

                // ✅ Deep link handling
                LaunchedEffect(Unit) {
                    handleNotificationDeepLink(mainNavController)
                }

                // ✅ UI STATE-BASED NAVIGATION
                when {
                    isLoggedIn -> {
                        Log.d("MainActivity_UI", "✅✅✅ SHOWING MAINSCAFFOLD (LOGGED IN)")
                        MainScaffold(
                            rootNavController = rootNavController,
                            mainNavController = mainNavController,
                            authViewModel = authViewModel  // ✅ pass Activity-scoped instance
                        )
                    }
                    else -> {
                        Log.d("MainActivity_UI", "❌❌❌ SHOWING AUTHNAVGRAPH (LOGGED OUT)")
                        AuthNavGraph(
                            navController = rootNavController,
                            authViewModel = authViewModel  // ✅ pass Activity-scoped instance
                        )
                    }
                }
            }
        }
    }

    // ✅ Deep link function
    private fun handleNotificationDeepLink(navController: NavHostController) {
        val postId = intent?.getStringExtra("postId")

        Log.d("MainActivity_DeepLink", "DeepLink PostId: $postId")

        if (!postId.isNullOrBlank()) {
            try {
                navController.navigate("photo_details/$postId")
            } catch (e: Exception) {
                Log.e("MainActivity_DeepLink", "Deep link error: ${e.message}")
            }
        }
    }

    // ✅ Handle notification click when app already open
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent

        Log.d("MainActivity_NewIntent", "New intent received")

        val postId = intent.getStringExtra("postId")

        if (!postId.isNullOrBlank() && mainNavController != null) {
            try {
                mainNavController?.navigate("photo_details/$postId")
            } catch (e: Exception) {
                Log.e("MainActivity_NewIntent", "onNewIntent error: ${e.message}")
            }
        }
    }
}