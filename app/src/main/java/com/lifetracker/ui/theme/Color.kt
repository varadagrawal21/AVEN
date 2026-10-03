package com.lifetracker.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Apple-inspired Black & Glass Theme
// Deep blacks, frosted glass surfaces, vibrant accent colors
// ─────────────────────────────────────────────────────────────────────────────

// Base blacks - True black for OLED, elevated surfaces
val BlackPure = Color(0xFF000000)
val BlackElevated1 = Color(0xFF1C1C1E)
val BlackElevated2 = Color(0xFF2C2C2E)
val BlackElevated3 = Color(0xFF3A3A3C)

// Glass surfaces - Semi-transparent frosted glass effect
// These are used with Modifier.background() for glassmorphism
val GlassUltraThin = Color(0x1AFFFFFF)   // 10% white - barely visible
val GlassThin = Color(0x26FFFFFF)        // 15% white - subtle
val GlassRegular = Color(0x33FFFFFF)     // 20% white - standard glass
val GlassThick = Color(0x4DFFFFFF)       // 30% white - prominent glass
val GlassHeavy = Color(0x66FFFFFF)       // 40% white - heavy glass

// Glass borders - Subtle white borders for definition
val GlassBorderSubtle = Color(0x1AFFFFFF)   // 10%
val GlassBorderDefault = Color(0x26FFFFFF)  // 15%
val GlassBorderStrong = Color(0x33FFFFFF)   // 20%

// Text colors on black/glass
val WhitePure = Color(0xFFFFFFFF)
val WhiteHigh = Color(0xE6FFFFFF)     // 90% - primary text
val WhiteMedium = Color(0x99FFFFFF)   // 60% - secondary text
val WhiteLow = Color(0x66FFFFFF)      // 40% - tertiary text
val WhiteMinimal = Color(0x33FFFFFF)  // 20% - disabled/hint

// Vibrant accent colors (Apple-style saturated hues)
val AccentBlue = Color(0xFF007AFF)      // iOS Blue
val AccentBlueLight = Color(0xFF5AC8FA) // Light Blue
val AccentGreen = Color(0xFF34C759)     // iOS Green
val AccentGreenLight = Color(0xFF30D158)
val AccentRed = Color(0xFFFF3B30)       // iOS Red
val AccentRedLight = Color(0xFFFF453A)
val AccentOrange = Color(0xFFFF9F0A)    // iOS Orange
val AccentOrangeLight = Color(0xFFFF9F0A)
val AccentYellow = Color(0xFFFFCC00)    // iOS Yellow
val AccentYellowLight = Color(0xFFFFD60A)
val AccentPurple = Color(0xFFAF52DE)    // iOS Purple
val AccentPurpleLight = Color(0xFFBF5AF2)
val AccentPink = Color(0xFFFF2D92)      // iOS Pink
val AccentPinkLight = Color(0xFFFF2D92)
val AccentTeal = Color(0xFF5AC8FA)      // iOS Teal
val AccentTealLight = Color(0xFF64D2FF)
val AccentIndigo = Color(0xFF5856D6)    // iOS Indigo
val AccentIndigoLight = Color(0xFF5E5CE6)
val AccentGray = Color(0xFF8E8E93)      // iOS Gray
val AccentGrayLight = Color(0xFFAEAEB2)

// Module accent colors - Updated to Apple-style vibrant hues
val HydrationBlue = AccentBlue
val HydrationBlueLight = AccentBlueLight
val HealthGreen = AccentGreen
val HealthGreenLight = AccentGreenLight
val FinanceGold = AccentYellow
val FinanceGoldLight = AccentYellowLight
val VocabPurple = AccentPurple
val VocabPurpleLight = AccentPurpleLight
val AcademicOrange = AccentOrange
val AcademicOrangeLight = AccentOrangeLight
val BooksIndigo = AccentIndigo
val BooksIndigoLight = AccentIndigoLight
val QuotesTeal = AccentTeal
val QuotesTealLight = AccentTealLight
val PeoplePink = AccentPink
val PeoplePinkLight = AccentPinkLight
val CustomGray = AccentGray
val CustomGrayLight = AccentGrayLight
val SettingsBlue = AccentBlue
val SettingsBlueLight = AccentBlueLight

// System semantic colors
val ErrorColor = AccentRed
val ErrorColorLight = AccentRedLight
val SuccessColor = AccentGreen
val SuccessColorLight = AccentGreenLight
val WarningColor = AccentOrange
val WarningColorLight = AccentOrangeLight

// Separators & dividers
val SeparatorOpaque = Color(0xFF38383A)
val SeparatorTranslucent = Color(0x33FFFFFF)

// Shadows for elevation
val ShadowColor = Color(0xFF000000)
val ShadowColorTransparent = Color(0x00000000)