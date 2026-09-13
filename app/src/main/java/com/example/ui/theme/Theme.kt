package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = WarmGoldenYellow,
    onPrimary = DeepCharcoal,
    primaryContainer = LightCreamYellow,
    onPrimaryContainer = DeepCharcoal,
    secondary = SoftBeige,
    onSecondary = DeepCharcoal,
    background = WarmOffWhite,
    onBackground = DeepCharcoal,
    surface = Color.White,
    onSurface = DeepCharcoal,
    surfaceVariant = SoftBeige,
    onSurfaceVariant = MutedTaupe
)

private val DarkColorScheme = darkColorScheme(
    primary = WarmGoldenYellow,
    onPrimary = DeepCharcoal,
    primaryContainer = WarmBrown,
    onPrimaryContainer = Color.White,
    secondary = DeepCharcoal,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = SoftBeige,
    surface = Color(0xFF1E1E1E),
    onSurface = SoftBeige,
    surfaceVariant = DeepCharcoal,
    onSurfaceVariant = MutedTaupe
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
