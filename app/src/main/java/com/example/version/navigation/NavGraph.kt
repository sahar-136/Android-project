package com.example.version.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.version.screens.LoginScreen
import com.example.version.screens.RegistrationScreen
import com.example.version.screens.FeedScreen
import com.example.version.screens.UploadScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FEED = "feed"
    const val UPLOAD = "upload"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onSignUpClick = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(Routes.FEED) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            RegistrationScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.FEED) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.FEED) {
            FeedScreen(
                navController = navController,

                onBackToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) // Clear entire navigation stack
                    }
                }
            )
        }
        composable(Routes.UPLOAD) {
            UploadScreen(navController)
        }
    }
}