package com.example.timewarp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PitchLightColorScheme = lightColorScheme(
    primary = PitchForest,
    onPrimary = Color.White,
    primaryContainer = PitchSage,
    onPrimaryContainer = PitchForestDark,
    secondary = PitchForest,
    onSecondary = Color.White,
    secondaryContainer = PitchSage,
    onSecondaryContainer = PitchForestDark,
    background = PitchCream,
    onBackground = PitchText,
    surface = PitchSurface,
    onSurface = PitchText,
    surfaceVariant = PitchSage,
    onSurfaceVariant = PitchMutedText,
    outline = PitchBorder,
    outlineVariant = PitchBorder,
    error = PitchError,
    onError = Color.White
)

private val PitchDarkColorScheme = darkColorScheme(
    primary = PitchLightGreen,
    onPrimary = PitchDarkBackground,
    primaryContainer = PitchForestDark,
    onPrimaryContainer = PitchDarkText,
    secondary = PitchLightGreen,
    onSecondary = PitchDarkBackground,
    secondaryContainer = Color(0xFF34483D),
    onSecondaryContainer = PitchDarkText,
    background = PitchDarkBackground,
    onBackground = PitchDarkText,
    surface = PitchDarkSurface,
    onSurface = PitchDarkText,
    surfaceVariant = Color(0xFF344038),
    onSurfaceVariant = PitchDarkMutedText,
    outline = PitchDarkBorder,
    outlineVariant = PitchDarkBorder,
    error = Color(0xFFFFB4A9),
    onError = Color(0xFF690005)
)

private val PitchShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TimewarpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        PitchDarkColorScheme
    } else {
        PitchLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PitchShapes,
        content = content
    )
}