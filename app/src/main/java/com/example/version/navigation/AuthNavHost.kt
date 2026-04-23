package com.example.version.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.version.screens.LoginScreen
import com.example.version.screens.RegistrationScreen

@Composable
fun AuthNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onSignUpClick = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    navController.popBackStack(Routes.LOGIN, inclusive = true)
                }
            )
        }
        composable(Routes.REGISTER) {
            RegistrationScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.popBackStack(Routes.LOGIN, inclusive = true)
                }
            )
        }
    }
}