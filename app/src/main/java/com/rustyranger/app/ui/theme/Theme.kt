package com.rustyranger.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = SoftBlue,
    tertiary = GoldFolderDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkBackground,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextMutedDark
)

val LightColorScheme = lightColorScheme(
    primary = AccentCyan,
    secondary = SoftBlue,
    tertiary = GoldFolder,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = LightBackground,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextMutedLight
)


@Composable
fun RustyRangerTheme(
    themeMode: String = "SYSTEM", // "DARK", "LIGHT", "SYSTEM"
    fontFamily: String = "SYSTEM", // "SYSTEM", "SANS_SERIF", "SERIF", "MONOSPACE"
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val selectedFontFamily = androidx.compose.runtime.remember(fontFamily) {
        when (fontFamily) {
            "SANS_SERIF" -> FontFamily.SansSerif
            "SERIF" -> FontFamily.Serif
            "MONOSPACE" -> FontFamily.Monospace
            "CURSIVE" -> FontFamily.Cursive
            "CASUAL" -> FontFamily(android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL))
            "CONDENSED" -> FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL))
            "THIN" -> FontFamily(android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL))
            "MEDIUM" -> FontFamily(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL))
            "LIGHT" -> FontFamily(android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL))
            "NOTO" -> FontFamily(android.graphics.Typeface.create("noto-sans", android.graphics.Typeface.NORMAL))
            "ELEGANT" -> FontFamily(android.graphics.Typeface.create("elegant", android.graphics.Typeface.NORMAL))
            else -> FontFamily.Default
        }
    }
    
    val customTypography = androidx.compose.runtime.remember(selectedFontFamily) {
        Typography(
            bodyLarge = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.2.sp
            ),
            bodySmall = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp
            ),
            titleMedium = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp
            ),
            titleSmall = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            )
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        content = content
    )
}
