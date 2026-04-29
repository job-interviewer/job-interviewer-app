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

/**
 * Applies the app's Material 3 light theme to the provided composable content.
 *
 * This sets the color scheme and typography used by MaterialTheme for any composables
 * rendered inside `content`.
 *
 * @param content Composable UI content that will be styled with the app's theme.
 */
@Composable
fun InterviewAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
