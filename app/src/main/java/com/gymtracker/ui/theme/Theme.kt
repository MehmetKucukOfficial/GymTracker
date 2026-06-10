package com.gymtracker.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val GymPrimary = Color(0xFFE53935)
val GymPrimaryDark = Color(0xFFB71C1C)
val GymSecondary = Color(0xFFFF7043)
val GymBackground = Color(0xFF121212)
val GymSurface = Color(0xFF1E1E1E)
val GymSurfaceVariant = Color(0xFF2C2C2C)
val GymOnSurface = Color(0xFFE0E0E0)
val GymOnBackground = Color(0xFFFFFFFF)

// Bitkinlik renkleri (mavi = dinlenmiş/az uyarılmış, kırmızı = çok yorgun)
val FatigueRested = Color(0xFF2196F3)     // Mavi - dinlenmiş / hafif uyarılmış
val FatigueLow = Color(0xFF4CAF50)        // Yeşil
val FatigueMedium = Color(0xFFFFEB3B)     // Sarı - orta
val FatigueHigh = Color(0xFFFF9800)       // Turuncu - yorgun
val FatigueMax = Color(0xFFF44336)        // Kırmızı - tam yorgun

private val DarkColorScheme = darkColorScheme(
    primary = GymPrimary,
    onPrimary = Color.White,
    secondary = GymSecondary,
    onSecondary = Color.White,
    background = GymBackground,
    onBackground = GymOnBackground,
    surface = GymSurface,
    onSurface = GymOnSurface,
    surfaceVariant = GymSurfaceVariant,
    onSurfaceVariant = Color(0xFFBDBDBD),
    error = Color(0xFFCF6679)
)

@Composable
fun GymTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
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
