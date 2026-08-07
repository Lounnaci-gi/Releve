package com.example.ade.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private val WaterLightColorScheme = lightColorScheme(
    primary = WaterPrimary,
    onPrimary = WaterOnPrimary,
    primaryContainer = WaterPrimaryContainer,
    onPrimaryContainer = WaterOnPrimaryContainer,
    secondary = WaterSecondary,
    onSecondary = WaterOnSecondary,
    secondaryContainer = WaterSecondaryContainer,
    onSecondaryContainer = WaterOnSecondaryContainer,
    tertiary = WaterTertiary,
    onTertiary = WaterOnTertiary,
    tertiaryContainer = WaterTertiaryContainer,
    onTertiaryContainer = WaterOnTertiaryContainer,
    error = WaterError,
    onError = WaterOnError,
    errorContainer = WaterErrorContainer,
    onErrorContainer = WaterOnErrorContainer,
    background = WaterBackground,
    onBackground = WaterOnBackground,
    surface = WaterSurface,
    onSurface = WaterOnSurface,
    surfaceVariant = WaterSurfaceVariant,
    onSurfaceVariant = WaterOnSurfaceVariant,
    outline = WaterOutline
)

@Composable
fun AdeReleveTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = WaterLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            activity?.window?.let { window ->
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
