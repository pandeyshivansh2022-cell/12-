package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ObsidianBlack,
    primaryContainer = CyberSurfaceElevated,
    onPrimaryContainer = NeonCyan,
    secondary = NeonElectricBlue,
    onSecondary = ObsidianBlack,
    secondaryContainer = CyberSurfaceDark,
    onSecondaryContainer = NeonSkyBlue,
    tertiary = NeonIndigo,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TextPrimaryDark,
    surface = DarkNavyBackground,
    onSurface = TextPrimaryDark,
    surfaceVariant = CyberSurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = CyberBorder,
    outlineVariant = CyberBorderGlowing.copy(alpha = 0.3f)
)

@Composable
fun GangstreamTheme(
    darkTheme: Boolean = true, // Force sleek dark mode by default
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = ObsidianBlack.toArgb()
                window.navigationBarColor = ObsidianBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for template backwards compatibility if needed
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    GangstreamTheme(darkTheme = true, content = content)
}
