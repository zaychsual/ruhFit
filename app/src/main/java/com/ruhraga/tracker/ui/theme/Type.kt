package com.ruhraga.tracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val RuhRagaFontFamily = FontFamily.SansSerif

/**
 * Custom metric & hero text styles defined in Ruh & Raga DESIGN.md.
 * Uses tabular figures ("tnum") on numeric styles so timers & rep counters never jitter.
 */
object RuhRagaTextStyles {
    val DisplayHero = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.8).sp,
        fontFeatureSettings = "tnum"
    )

    val DisplayHeroMobile = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.64).sp,
        fontFeatureSettings = "tnum"
    )

    val MetricNumLg = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.36).sp,
        fontFeatureSettings = "tnum"
    )

    val MetricNumMd = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        fontFeatureSettings = "tnum"
    )

    val TabularBodyMd = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = "tnum"
    )
}

val RuhRagaTypography = Typography(
    displayLarge = RuhRagaTextStyles.DisplayHero,
    displayMedium = RuhRagaTextStyles.DisplayHeroMobile,
    displaySmall = RuhRagaTextStyles.MetricNumLg,
    headlineLarge = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.42).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.22).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp
    ),
    labelSmall = TextStyle(
        fontFamily = RuhRagaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.44.sp
    )
)
