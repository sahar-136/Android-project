package com.example.version.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.version.screens.LoginScreen
import com.example.version.screens.RegistrationScreen
import com.example.version.screens.ResetPasswordScreen

@Composable
fun AuthNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onSignUpClick = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginSuccess = {
                    Log.d("NAV_DEBUG", "✅ Login Success - ViewModel already updated isLoggedIn")
                    // ✅ ViewModel has already set isLoggedIn = true
                    // ✅ MainActivity will automatically switch to MainScaffold
                },
                navController = navController
            )
        }

        composable(Routes.REGISTER) {
            RegistrationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    Log.d("NAV_DEBUG", "✅ Register Success - ViewModel already updated isLoggedIn")
                    // ✅ ViewModel has already set isLoggedIn = true
                    // ✅ MainActivity will automatically switch to MainScaffold
                }
            )
        }

        composable(Routes.RESET_PASSWORD) {
            ResetPasswordScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}