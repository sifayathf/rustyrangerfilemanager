package com.rustyranger.app.ui.theme

import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF12131C)
val DarkSurface = Color(0xFF1A1C29)
val DarkSurfaceVariant = Color(0xFF242738)

val LightBackground = Color(0xFFF0F2F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE4E7ED)

val AccentCyan = Color(0xFF41A6A6)
val SoftBlue = Color(0xFF3B82F6) // A bit darker blue for light mode readability, or keep same
val GoldFolder = Color(0xFFFFCA28) // Vibrant yellow for Light Mode (Material Amber 400)
val GoldFolderDark = Color(0xFFFFD54F) // Vibrant yellow for Dark Mode (Material Amber 300)

val TextPrimaryDark = Color(0xFFD6DAE6)
val TextSecondaryDark = Color(0xFFA0A6B9)
val TextMutedDark = Color(0xFF787E91)

val TextPrimaryLight = Color(0xFF1A1A1A)
val TextSecondaryLight = Color(0xFF4A4A4A)
val TextMutedLight = Color(0xFF6B7280)

val StatusOk = Color(0xFF89DCA5)
val StatusWarn = Color(0xFFF0C674)
val StatusError = Color(0xFFF07878)

// Legacy Aliases to prevent compilation errors in older components
val TextPrimary = TextPrimaryDark
val TextSecondary = TextSecondaryDark
val TextMuted = TextMutedDark
