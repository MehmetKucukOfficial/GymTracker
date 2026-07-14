package com.gymtracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val GymPrimary = Color(0xFFC73532)
val GymPrimaryDark = Color(0xFFFFB3AE)
val GymSecondary = Color(0xFF9A4521)
val GymBackground = Color(0xFF121315)
val GymSurface = Color(0xFF1A1C1E)
val GymSurfaceVariant = Color(0xFF292B2E)
val GymOnSurface = Color(0xFFE4E2E2)
val GymOnBackground = Color(0xFFECE0DF)

// Bitkinlik renkleri (mavi = dinlenmiş/az uyarılmış, kırmızı = çok yorgun)
val FatigueRested = Color(0xFF2196F3)     // Mavi - dinlenmiş / hafif uyarılmış
val FatigueLow = Color(0xFF4CAF50)        // Yeşil
val FatigueMedium = Color(0xFFFFEB3B)     // Sarı - orta
val FatigueHigh = Color(0xFFFF9800)       // Turuncu - yorgun
val FatigueMax = Color(0xFFF44336)        // Kırmızı - tam yorgun

private val DarkColorScheme = darkColorScheme(
    primary = GymPrimaryDark,
    onPrimary = Color(0xFF680009),
    primaryContainer = Color(0xFF8F171B),
    onPrimaryContainer = Color(0xFFFFDAD7),
    secondary = Color(0xFFFFB693),
    onSecondary = Color(0xFF572000),
    secondaryContainer = Color(0xFF7B2F0D),
    onSecondaryContainer = Color(0xFFFFDBCB),
    tertiary = Color(0xFF83D5C5),
    onTertiary = Color(0xFF003731),
    background = GymBackground,
    onBackground = GymOnBackground,
    surface = GymSurface,
    onSurface = GymOnSurface,
    surfaceVariant = GymSurfaceVariant,
    onSurfaceVariant = Color(0xFFC9C5C6),
    outline = Color(0xFF938F90),
    outlineVariant = Color(0xFF494647),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = GymPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD7),
    onPrimaryContainer = Color(0xFF410004),
    secondary = GymSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCB),
    onSecondaryContainer = Color(0xFF351000),
    tertiary = Color(0xFF006B60),
    onTertiary = Color.White,
    background = Color(0xFFFFF8F7),
    onBackground = Color(0xFF201A1A),
    surface = Color(0xFFFFF8F7),
    onSurface = Color(0xFF201A1A),
    surfaceVariant = Color(0xFFF4DDDC),
    onSurfaceVariant = Color(0xFF534343),
    outline = Color(0xFF857372),
    outlineVariant = Color(0xFFD8C2C1),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun GymTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = GtTypography,
        shapes = GtShapes,
        content = content
    )
}

fun fatigueColor(fatigue: Float): Color {
    val f = fatigue.coerceIn(0f, 1f)
    val stops = listOf(0.0f to FatigueRested, 0.25f to FatigueLow, 0.5f to FatigueMedium, 0.75f to FatigueHigh, 1.0f to FatigueMax)
    for (i in 0 until stops.size - 1) {
        val (startPct, startColor) = stops[i]
        val (endPct, endColor) = stops[i + 1]
        if (f <= endPct) {
            val localFraction = (f - startPct) / (endPct - startPct)
            return lerp(startColor, endColor, localFraction)
        }
    }
    return FatigueMax
}
