package com.mgneg.morpheui.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Morphe brand gradient endpoints. */
val MorpheBrandBlue = Color(0xFF1E5AA8)
val MorpheBrandTeal = Color(0xFF00AFAE)

private val LightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF005FAC),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD4E3FF),
    onPrimaryContainer = Color(0xFF001C39),
    secondary = Color(0xFF545F71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD8E3F8),
    onSecondaryContainer = Color(0xFF111C2B),
    tertiary = Color(0xFF6D5677),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF6D9FF),
    onTertiaryContainer = Color(0xFF271430),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFDFCFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C6CF),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFFA4C9FF),
    surfaceTint = Color(0xFF005FAC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F3F7),
    surfaceContainer = Color(0xFFEEEDF1),
    surfaceContainerHigh = Color(0xFFE9E8EC),
    surfaceContainerHighest = Color(0xFFE3E2E6),
    surfaceBright = Color(0xFFFAF9FD),
    surfaceDim = Color(0xFFDAD9DD),
    scrim = Color(0xFF000000)
)

private val DarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFA4C9FF),
    onPrimary = Color(0xFF00315D),
    primaryContainer = Color(0xFF004884),
    onPrimaryContainer = Color(0xFFD4E3FF),
    secondary = Color(0xFFBCC7DB),
    onSecondary = Color(0xFF263141),
    secondaryContainer = Color(0xFF3D4758),
    onSecondaryContainer = Color(0xFFD8E3F8),
    tertiary = Color(0xFFD9BDE3),
    onTertiary = Color(0xFF3D2946),
    tertiaryContainer = Color(0xFF543F5E),
    onTertiaryContainer = Color(0xFFF6D9FF),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C6CF),
    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF43474E),
    inverseSurface = Color(0xFFE3E2E6),
    inverseOnSurface = Color(0xFF1A1C1E),
    inversePrimary = Color(0xFF005FAC),
    surfaceTint = Color(0xFFA4C9FF),
    surfaceContainerLowest = Color(0xFF0C0E11),
    surfaceContainerLow = Color(0xFF1A1C1E),
    surfaceContainer = Color(0xFF1E2022),
    surfaceContainerHigh = Color(0xFF282A2C),
    surfaceContainerHighest = Color(0xFF333537),
    surfaceBright = Color(0xFF37393C),
    surfaceDim = Color(0xFF111416),
    scrim = Color(0xFF000000)
)

/** Extra semantic colors the reference theme defines alongside the M3 scheme. */
data class MorpheExtraColors(val warning: Color, val warningContainer: Color, val onWarningContainer: Color, val success: Color, val successContainer: Color, val onSuccessContainer: Color)

@Composable
fun morpheExtraColors(dark: Boolean): MorpheExtraColors = if (dark) {
    MorpheExtraColors(Color(0xFFF5BD48), Color(0xFF5F4100), Color(0xFFFFDEA8), Color(0xFF90D889), Color(0xFF0D5316), Color(0xFFABF5A3))
} else {
    MorpheExtraColors(Color(0xFF7C5800), Color(0xFFFFDEA8), Color(0xFF271900), Color(0xFF286C2A), Color(0xFFABF5A3), Color(0xFF002204))
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun MorpheTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
