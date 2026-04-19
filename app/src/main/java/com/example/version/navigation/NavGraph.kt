package com.example.version.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.version.screens.*
import com.example.version.viewmodel.AuthViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "resetPassword"
    const val HOME = "home"
    const val SEARCH = "search"
    const val UPLOAD = "upload"
    const val EDIT_PROFILE = "edit_profile"
    const val PROFILE = "profile/{userId}"
    const val PHOTO_DETAILS = "photoDetails/{postId}"
    const val COMMENTS = "comments/{postId}"

    fun profile(userId: String) = "profile/$userId"
    fun photoDetails(postId: String) = "photoDetails/$postId"
    fun comments(postId: String) = "comments/$postId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isLoggedIn = authViewModel.isLoggedIn.observeAsState(false).value
    val startDestination = if (isLoggedIn) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login
        composable(Routes.LOGIN) {
            LoginScreen(
                onSignUpClick = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        // Register
        composable(Routes.REGISTER) {
            RegistrationScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        // Reset Password (optional: implement when ready)
        composable(Routes.RESET_PASSWORD) {
            // TODO: ResetPasswordScreen()
        }

        // Home
        composable(Routes.HOME) {
            HomeScreen(navController)
        }
        // Search
        composable(Routes.SEARCH) {
            SearchScreen(navController = navController)
        }
        // Upload
        composable(Routes.UPLOAD) {
            UploadScreen(navController)
        }
        // Profile by userId
        composable(
            route = Routes.PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(
                navController = navController,
                userId = userId,
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) }
            )
        }
        // Edit Profile (current user)
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(navController = navController)
        }
        // Photo Details by postId
        composable(
            route = Routes.PHOTO_DETAILS,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PhotoDetailsScreen(postId = postId, navController = navController)
        }
        // Comments by postId
        composable(
            route = Routes.COMMENTS,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            CommentsScreen(postId = postId, navController = navController)
        }
    }
}