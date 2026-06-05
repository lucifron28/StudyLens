package com.example.modulelensmobile.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ModuleNavy,
    onPrimary = ModuleCard,
    primaryContainer = ModuleTeal,
    onPrimaryContainer = ModuleNavy,
    secondary = ModuleTealDark,
    onSecondary = ModuleCard,
    secondaryContainer = ModuleTeal.copy(alpha = 0.45f),
    tertiary = ModuleActionBlue,
    background = ModuleBackground,
    onBackground = ModuleTextPrimary,
    surface = ModuleCard,
    onSurface = ModuleTextPrimary,
    surfaceVariant = ModuleMutedSurface,
    onSurfaceVariant = ModuleTextSecondary,
    outline = ModuleTextSecondary.copy(alpha = 0.35f),
    outlineVariant = ModuleMutedSurface,
    error = ModuleError,
    onError = Color.White,
)

@Composable
fun ModuleLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
