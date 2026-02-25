package com.example.version

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.version.navigation.AppNavGraph
import com.example.version.ui.theme.VersionTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VersionTheme {
                Surface {
                    val navController = rememberNavController()
                    AppNavGraph(navController)
                }
            }
        }
    }
}