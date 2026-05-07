package com.example.version.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.version.screens.LoginScreen
import com.example.version.screens.RegistrationScreen
import com.example.version.screens.ResetPasswordScreen
import com.example.version.viewmodel.AuthViewModel

@Composable
fun AuthNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel  // ✅ Activity-scoped instance passed from MainActivity
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
                    Log.d("NAV_DEBUG", "✅ Login Success - isLoggedIn updated on Activity-scoped VM")
                },
                navController = navController,
                viewModel = authViewModel  // ✅ override default hiltViewModel() with shared instance
            )
        }

        composable(Routes.REGISTER) {
            RegistrationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    Log.d("NAV_DEBUG", "✅ Register Success - isLoggedIn updated on Activity-scoped VM")
                },
                viewModel = authViewModel  // ✅ override default hiltViewModel() with shared instance
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