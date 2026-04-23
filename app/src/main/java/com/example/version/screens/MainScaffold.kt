package com.example.version.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.version.navigation.Routes
import com.example.version.ui.theme.AppColors
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    rootNavController: NavHostController,
    mainNavController: NavHostController
) {
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in setOf(
        Routes.HOME,
        Routes.SEARCH,
        Routes.UPLOAD,
        Routes.EDIT_PROFILE
    ) || (currentRoute?.startsWith("profile/") == true)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = AppColors.BackgroundWhite) {

                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            mainNavController.navigate(Routes.HOME) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(Routes.HOME) { saveState = true }
                            }
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )

                    NavigationBarItem(
                        selected = currentRoute == Routes.SEARCH,
                        onClick = {
                            mainNavController.navigate(Routes.SEARCH) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(Routes.HOME) { saveState = true }
                            }
                        },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        label = { Text("Search") }
                    )

                    NavigationBarItem(
                        selected = currentRoute == Routes.UPLOAD,
                        onClick = {
                            mainNavController.navigate(Routes.UPLOAD) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(Routes.HOME) { saveState = true }
                            }
                        },
                        icon = { Icon(Icons.Filled.Add, contentDescription = "Upload") },
                        label = { Text("Upload") }
                    )

                    NavigationBarItem(
                        selected = currentRoute?.startsWith("profile/") == true,
                        onClick = {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid != null) {
                                mainNavController.navigate(Routes.profile(uid)) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(Routes.HOME) { saveState = true }
                                }
                            } else {
                                rootNavController.navigate(Routes.LOGIN) {
                                    popUpTo(0)
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = mainNavController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.HOME) { HomeScreen(navController = mainNavController) }
            composable(Routes.SEARCH) { SearchScreen(navController = mainNavController) }
            composable(Routes.UPLOAD) { UploadScreen(navController = mainNavController) }

            composable(
                route = Routes.PROFILE,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId").orEmpty()
                ProfileScreen(
                    navController = mainNavController,
                    userId = userId,
                    onEditProfile = { mainNavController.navigate(Routes.EDIT_PROFILE) }
                )
            }

            composable(Routes.EDIT_PROFILE) {
                EditProfileScreen(navController = mainNavController)
            }

            composable(
                route = Routes.PHOTO_DETAILS,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId").orEmpty()
                PhotoDetailsScreen(postId = postId, navController = mainNavController)
            }

            composable(
                route = Routes.COMMENTS,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId").orEmpty()
                CommentsScreen(postId = postId, navController = mainNavController)
            }
        }
    }
}
