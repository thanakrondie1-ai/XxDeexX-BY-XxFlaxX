package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = StudioPrimary,
    onPrimary = StudioOnPrimary,
    primaryContainer = StudioPrimaryContainer,
    onPrimaryContainer = StudioOnPrimaryContainer,
    secondary = StudioSecondary,
    onSecondary = StudioOnSecondary,
    secondaryContainer = StudioSecondaryContainer,
    onSecondaryContainer = StudioOnSecondaryContainer,
    tertiary = StudioTertiary,
    onTertiary = StudioOnTertiary,
    tertiaryContainer = StudioTertiaryContainer,
    onTertiaryContainer = StudioOnTertiaryContainer,
    background = StudioBackgroundDark,
    onBackground = StudioOnBackgroundDark,
    surface = StudioSurfaceDark,
    onSurface = StudioOnSurfaceDark,
    surfaceVariant = StudioSurfaceVariantDark,
    onSurfaceVariant = StudioOnSurfaceVariantDark,
    outline = StudioOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = StudioPrimary,
    onPrimary = StudioOnPrimary,
    primaryContainer = StudioPrimaryContainer,
    onPrimaryContainer = StudioOnPrimaryContainer,
    secondary = StudioSecondary,
    onSecondary = StudioOnSecondary,
    secondaryContainer = StudioSecondaryContainer,
    onSecondaryContainer = StudioOnSecondaryContainer,
    tertiary = StudioTertiary,
    onTertiary = StudioOnTertiary,
    tertiaryContainer = StudioTertiaryContainer,
    onTertiaryContainer = StudioOnTertiaryContainer,
    background = StudioBackgroundLight,
    onBackground = StudioOnBackgroundLight,
    surface = StudioSurfaceLight,
    onSurface = StudioOnSurfaceLight,
    surfaceVariant = StudioSurfaceVariantLight,
    onSurfaceVariant = StudioOnSurfaceVariantLight,
    outline = StudioOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve crafted studio aesthetics by default
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
        typography = Typography,
        content = content
    )
}
