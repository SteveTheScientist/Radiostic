package com.radiostic.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Expressive Amber Dark Palette
val AmberPrimary = Color(0xFFFFB951)
val AmberOnPrimary = Color(0xFF452B00)
val AmberPrimaryContainer = Color(0xFF633F00)
val AmberOnPrimaryContainer = Color(0xFFFFDDB3)

val AmberSecondary = Color(0xFFE0C1A3)
val AmberOnSecondary = Color(0xFF402D17)
val AmberSecondaryContainer = Color(0xFF58432B)
val AmberOnSecondaryContainer = Color(0xFFFDE7CF)

val AmberTertiary = Color(0xFFBDCC99)
val AmberOnTertiary = Color(0xFF28340F)
val AmberTertiaryContainer = Color(0xFF3E4B23)
val AmberOnTertiaryContainer = Color(0xFFD9E8B3)

val AmberSurface = Color(0xFF16130E)
val AmberOnSurface = Color(0xFFEBE1D7)
val AmberOnSurfaceVariant = Color(0xFFD5C4B4)

val AmberSurfaceContainerLowest = Color(0xFF110E0A)
val AmberSurfaceContainerLow = Color(0xFF1F1B16)
val AmberSurfaceContainer = Color(0xFF231F1A)
val AmberSurfaceContainerHigh = Color(0xFF2E2924)
val AmberSurfaceContainerHighest = Color(0xFF39342E)

val AmberOutline = Color(0xFF9E8E80)
val AmberOutlineVariant = Color(0xFF514539)

val AmberInverseSurface = Color(0xFFEBE1D7)
val AmberInverseOnSurface = Color(0xFF342F29)
val AmberInversePrimary = Color(0xFF835500)

val AmberError = Color(0xFFFFB4AB)
val AmberOnError = Color(0xFF690005)
val AmberErrorContainer = Color(0xFF93000A)
val AmberOnErrorContainer = Color(0xFFFFDAD6)

val RadiosticDarkColorScheme: ColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = AmberOnPrimary,
    primaryContainer = AmberPrimaryContainer,
    onPrimaryContainer = AmberOnPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = AmberOnSecondaryContainer,
    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = AmberOnTertiaryContainer,
    surface = AmberSurface,
    onSurface = AmberOnSurface,
    surfaceVariant = AmberSurfaceContainerHigh,
    onSurfaceVariant = AmberOnSurfaceVariant,
    surfaceContainerLowest = AmberSurfaceContainerLowest,
    surfaceContainerLow = AmberSurfaceContainerLow,
    surfaceContainer = AmberSurfaceContainer,
    surfaceContainerHigh = AmberSurfaceContainerHigh,
    surfaceContainerHighest = AmberSurfaceContainerHighest,
    outline = AmberOutline,
    outlineVariant = AmberOutlineVariant,
    inverseSurface = AmberInverseSurface,
    inverseOnSurface = AmberInverseOnSurface,
    inversePrimary = AmberInversePrimary,
    error = AmberError,
    onError = AmberOnError,
    errorContainer = AmberErrorContainer,
    onErrorContainer = AmberOnErrorContainer
)

val RadiosticLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF835500),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDDB3),
    onPrimaryContainer = Color(0xFF2A1800),
    secondary = Color(0xFF715A42),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFDE7CF),
    onSecondaryContainer = Color(0xFF281806),
    tertiary = Color(0xFF556338),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD9E8B3),
    onTertiaryContainer = Color(0xFF141F00),
    surface = Color(0xFFFFF8F3),
    onSurface = Color(0xFF201B14),
    surfaceVariant = Color(0xFFF2E0D0),
    onSurfaceVariant = Color(0xFF514539),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF2E9),
    surfaceContainer = Color(0xFFF4ECE3),
    surfaceContainerHigh = Color(0xFFEEE6DD),
    surfaceContainerHighest = Color(0xFFE8E0D7),
    outline = Color(0xFF837567),
    outlineVariant = Color(0xFFD5C4B4),
    inverseSurface = Color(0xFF352F28),
    inverseOnSurface = Color(0xFFF9EFE6),
    inversePrimary = Color(0xFFFFB951),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)
