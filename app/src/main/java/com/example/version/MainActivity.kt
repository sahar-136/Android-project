package com.example.version

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.version.navigation.AuthNavGraph
import com.example.version.screens.MainScaffold
import com.example.version.ui.theme.VersionTheme
import com.example.version.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VersionTheme {
                val rootNavController = rememberNavController()
                val mainNavController = rememberNavController() // ✅ created at Activity level

                val authViewModel: AuthViewModel = hiltViewModel()
                val isLoggedIn by authViewModel.isLoggedIn.observeAsState(false)

                if (isLoggedIn) {
                    MainScaffold(
                        rootNavController = rootNavController,
                        mainNavController = mainNavController
                    )
                } else {
                    AuthNavGraph(navController = rootNavController)
                }
            }
        }
    }
}