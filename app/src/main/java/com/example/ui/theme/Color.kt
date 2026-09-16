package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Concrete Dark Palette Values
val PaletteDarkBg = Color(0xFF080808)
val PaletteDarkCarbon = Color(0xFF121212)
val PaletteDarkPanel = Color(0xFF171717)
val PaletteDarkSurface = Color(0xFF1E1E1E)
val PaletteDarkBorder = Color(0xFF2A2A2A)
val PaletteDarkTextPrimary = Color(0xFFF8FAFC)
val PaletteDarkTextMuted = Color(0xFFA1A1AA)

// Concrete Light Palette Values (Soft warm off-white, optimal readability during day)
val PaletteLightBg = Color(0xFFF8FAFC)
val PaletteLightCarbon = Color(0xFFF1F5F9)
val PaletteLightPanel = Color(0xFFFFFFFF)
val PaletteLightSurface = Color(0xFFF8FAFC)
val PaletteLightBorder = Color(0xFFE2E8F0)
val PaletteLightTextPrimary = Color(0xFF0F172A)
val PaletteLightTextMuted = Color(0xFF64748B)

// Brand Colors (Signature Max Seg Identity)
val MaxBordo = Color(0xFF800020)
val MaxBordoLight = Color(0xFFA31236)
val MaxGold = Color(0xFFD4AF37)
val MaxGoldLight = Color(0xFFF3E5AB)

val AccentSuccess = Color(0xFF34D399)
val AccentBlue = Color(0xFF60A5FA)
val AccentRed = Color(0xFFF87171)
val AccentPurple = Color(0xFFC084FC)

data class MaxSegThemeColors(
    val isDark: Boolean,
    val bg: Color,
    val carbon: Color,
    val panel: Color,
    val panelSurface: Color,
    val border: Color,
    val textPrimary: Color,
    val textMuted: Color
)

val DarkThemeColors = MaxSegThemeColors(
    isDark = true,
    bg = PaletteDarkBg,
    carbon = PaletteDarkCarbon,
    panel = PaletteDarkPanel,
    panelSurface = PaletteDarkSurface,
    border = PaletteDarkBorder,
    textPrimary = PaletteDarkTextPrimary,
    textMuted = PaletteDarkTextMuted
)

val LightThemeColors = MaxSegThemeColors(
    isDark = false,
    bg = PaletteLightBg,
    carbon = PaletteLightCarbon,
    panel = PaletteLightPanel,
    panelSurface = PaletteLightSurface,
    border = PaletteLightBorder,
    textPrimary = PaletteLightTextPrimary,
    textMuted = PaletteLightTextMuted
)

val LocalMaxSegColors = staticCompositionLocalOf { DarkThemeColors }

// Dynamic theme-aware properties for seamless backwards compatibility across all screens
val BgDark: Color
    @Composable
    get() = LocalMaxSegColors.current.bg

val CarbonDark: Color
    @Composable
    get() = LocalMaxSegColors.current.carbon

val PanelDark: Color
    @Composable
    get() = LocalMaxSegColors.current.panel

val PanelSurface: Color
    @Composable
    get() = LocalMaxSegColors.current.panelSurface

val BorderDark: Color
    @Composable
    get() = LocalMaxSegColors.current.border

val TextWhite: Color
    @Composable
    get() = LocalMaxSegColors.current.textPrimary

val TextMuted: Color
    @Composable
    get() = LocalMaxSegColors.current.textMuted
