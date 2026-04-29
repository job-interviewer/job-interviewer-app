package com.interview.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Navy40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = NavyGrey80,
    secondary = Blue40,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

@Composable
fun InterviewAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
