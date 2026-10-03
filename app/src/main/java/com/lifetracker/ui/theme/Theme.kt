package com.lifetracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = WhitePure,
    primaryContainer = BlackElevated2,
    onPrimaryContainer = WhiteHigh,
    secondary = AccentPurple,
    onSecondary = WhitePure,
    secondaryContainer = BlackElevated2,
    onSecondaryContainer = WhiteHigh,
    tertiary = AccentGreen,
    onTertiary = WhitePure,
    tertiaryContainer = BlackElevated2,
    onTertiaryContainer = WhiteHigh,
    error = ErrorColor,
    onError = WhitePure,
    errorContainer = BlackElevated2,
    onErrorContainer = WhiteHigh,
    background = BlackPure,
    onBackground = WhiteHigh,
    surface = BlackElevated1,
    onSurface = WhiteHigh,
    surfaceVariant = BlackElevated2,
    onSurfaceVariant = WhiteMedium,
    surfaceContainerLow = BlackElevated1,
    surfaceContainer = BlackElevated2,
    surfaceContainerHigh = BlackElevated3,
    surfaceContainerHighest = BlackElevated3,
    outline = GlassBorderDefault,
    outlineVariant = GlassBorderSubtle,
    inverseSurface = WhitePure,
    inverseOnSurface = BlackPure,
    inversePrimary = AccentBlue,
    scrim = ShadowColor
)

@Composable
fun LifeTrackerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BlackPure.toArgb()
            window.navigationBarColor = BlackPure.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LifeTrackerTypography,
        content = content
    )
}

enum class GlassElevation {
    UltraThin,
    Thin,
    Medium,
    Thick,
    Heavy
}

object GlassShapes {
    val Small = RoundedCornerShape(12.dp)
    val Medium = RoundedCornerShape(16.dp)
    val Large = RoundedCornerShape(20.dp)
    val ExtraLarge = RoundedCornerShape(28.dp)
    val Full = RoundedCornerShape(999.dp)
    val TopLarge = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    val BottomLarge = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
}

@Composable
fun glassBackgroundColor(elevation: GlassElevation): Color = when (elevation) {
    GlassElevation.UltraThin -> GlassUltraThin
    GlassElevation.Thin -> GlassThin
    GlassElevation.Medium -> GlassRegular
    GlassElevation.Thick -> GlassThick
    GlassElevation.Heavy -> GlassHeavy
}

@Composable
fun glassBorderColor(elevation: GlassElevation): Color = when (elevation) {
    GlassElevation.UltraThin -> WhitePure.copy(alpha = 0.08f)
    GlassElevation.Thin -> WhitePure.copy(alpha = 0.12f)
    GlassElevation.Medium -> WhitePure.copy(alpha = 0.15f)
    GlassElevation.Thick -> WhitePure.copy(alpha = 0.2f)
    GlassElevation.Heavy -> WhitePure.copy(alpha = 0.25f)
}

@Composable
fun Modifier.glassSurface(
    elevation: GlassElevation = GlassElevation.Medium,
    shape: Shape = RoundedCornerShape(16.dp),
    border: Boolean = true,
    borderAlpha: Float = 0f
): Modifier {
    val containerColor = glassBackgroundColor(elevation)
    val borderColor = if (borderAlpha > 0f) WhitePure.copy(alpha = borderAlpha) else glassBorderColor(elevation)

    return this
        .clip(shape)
        .background(containerColor)
        .then(
            if (border) {
                Modifier.border(0.5.dp, borderColor, shape)
            } else {
                Modifier
            }
        )
}

@Composable
fun Modifier.glassCard(
    elevation: GlassElevation = GlassElevation.Medium,
    shape: Shape = RoundedCornerShape(20.dp),
    padding: androidx.compose.ui.unit.Dp = 16.dp
): Modifier {
    return this
        .padding(padding)
        .glassSurface(elevation, shape)
}

@Composable
fun Modifier.glassButton(
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier {
    val containerColor = if (enabled) GlassRegular else GlassUltraThin
    val borderColor = if (enabled) WhitePure.copy(alpha = 0.15f) else WhitePure.copy(alpha = 0.08f)

    return this
        .clip(shape)
        .background(containerColor)
        .border(0.5.dp, borderColor, shape)
}
