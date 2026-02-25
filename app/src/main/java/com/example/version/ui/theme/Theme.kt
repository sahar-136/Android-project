package com.example.version.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// All color values will come from Colors.kt

private val DarkColorScheme = darkColorScheme(
    primary = BlueStart,
    secondary = BlueCyan,
    background = BlueStart,
    surface = BlueStart,
    onPrimary = ButtonTextWhite,
    onSecondary = ButtonTextWhite,
    onBackground = ButtonTextWhite,
    onSurface = ButtonTextWhite,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = BlueMiddle,
    secondary = BlueCyan,
    background = Color.White,
    surface = TextFieldBackground,
    onPrimary = ButtonTextWhite,
    onSecondary = ButtonTextWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
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