package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SalimDarkBlue,
    onPrimary = SalimWhite,
    primaryContainer = SalimDarkSurfaceSubtle,
    onPrimaryContainer = SalimDarkTextPrimary,
    background = SalimDarkBg,
    onBackground = SalimDarkTextPrimary,
    surface = SalimDarkSurface,
    onSurface = SalimDarkTextPrimary,
    surfaceVariant = SalimDarkSurfaceSubtle,
    onSurfaceVariant = SalimDarkTextSecondary,
    outline = SalimDarkDivider,
    error = SalimDestructive,
    onError = SalimWhite
)

private val LightColorScheme = lightColorScheme(
    primary = SalimBlue,
    onPrimary = SalimWhite,
    primaryContainer = SalimBlueLight,
    onPrimaryContainer = SalimBlue,
    background = SalimWhite,
    onBackground = SalimBlack,
    surface = SalimSurface,
    onSurface = SalimBlack,
    surfaceVariant = SalimSurfaceSubtle,
    onSurfaceVariant = SalimTextSecondary,
    outline = SalimDivider,
    error = SalimDestructive,
    onError = SalimWhite
)

@Composable
fun SalimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

