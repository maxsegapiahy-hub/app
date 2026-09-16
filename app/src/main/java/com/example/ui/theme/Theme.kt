package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MaxGold,
    onPrimary = PaletteDarkBg,
    primaryContainer = MaxBordoLight,
    onPrimaryContainer = PaletteDarkTextPrimary,
    secondary = MaxBordo,
    onSecondary = PaletteDarkTextPrimary,
    tertiary = AccentBlue,
    onTertiary = PaletteDarkTextPrimary,
    background = PaletteDarkBg,
    onBackground = PaletteDarkTextPrimary,
    surface = PaletteDarkPanel,
    onSurface = PaletteDarkTextPrimary,
    surfaceVariant = PaletteDarkSurface,
    onSurfaceVariant = PaletteDarkTextMuted,
    outline = PaletteDarkBorder,
    error = AccentRed,
    onError = PaletteDarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = MaxGold,
    onPrimary = PaletteLightBg,
    primaryContainer = MaxGoldLight,
    onPrimaryContainer = MaxBordo,
    secondary = MaxBordo,
    onSecondary = PaletteLightBg,
    tertiary = AccentBlue,
    onTertiary = PaletteLightBg,
    background = PaletteLightBg,
    onBackground = PaletteLightTextPrimary,
    surface = PaletteLightPanel,
    onSurface = PaletteLightTextPrimary,
    surfaceVariant = PaletteLightSurface,
    onSurfaceVariant = PaletteLightTextMuted,
    outline = PaletteLightBorder,
    error = AccentRed,
    onError = PaletteLightBg
)

@Composable
fun MaxSegTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val maxColors = if (darkTheme) DarkThemeColors else LightThemeColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = maxColors.bg.toArgb()
                window.navigationBarColor = maxColors.bg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalMaxSegColors provides maxColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
