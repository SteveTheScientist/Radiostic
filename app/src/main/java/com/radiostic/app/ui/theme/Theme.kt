package com.radiostic.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun RadiosticTheme(
    selectedFont: String = "System Font",
    customFontFilePath: String? = null,
    useDynamicColor: Boolean = false,
    themeMode: String = "Dark",
    textSize: String = "Default",
    boldText: Boolean = false,
    cornerRoundness: String = "Default",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    val isDark = when (themeMode.lowercase()) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }

    val colorScheme: ColorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> RadiosticDarkColorScheme
        else -> RadiosticLightColorScheme
    }

    val fontFamily = getFontFamily(selectedFont, customFontFilePath)
    val typography = createRadiosticTypography(
        fontFamily = fontFamily,
        textSizeSetting = textSize,
        boldTextSetting = boldText
    )
    val shapes = createRadiosticShapes(cornerRoundness)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
