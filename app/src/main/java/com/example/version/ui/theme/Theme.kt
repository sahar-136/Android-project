package com.example.version.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Using AppColors from Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = AppColors.PrimaryOrange,
    secondary = AppColors.LightOrange,
    background = AppColors.BlackText,
    surface = AppColors.DarkGray,
    onPrimary = AppColors.ButtonTextWhite,
    onSecondary = AppColors.ButtonTextWhite,
    onBackground = AppColors.ButtonTextWhite,
    onSurface = AppColors.ButtonTextWhite,
    error = AppColors.ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.PrimaryOrange,
    secondary = AppColors.LightOrange,
    background = AppColors.BackgroundWhite,
    surface = AppColors.LightGray,
    onPrimary = AppColors.ButtonTextWhite,
    onSecondary = AppColors.ButtonTextWhite,
    onBackground = AppColors.BlackText,
    onSurface = AppColors.BlackText,
    error = AppColors.ErrorRed
)

@Composable
fun VersionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // From Typography.kt
        content = content
    )
}