package com.example.version

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun VersionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        // MaterialTheme default values, ya apni colors, typography yahan configure kar sakte ho
        content = content
    )
}