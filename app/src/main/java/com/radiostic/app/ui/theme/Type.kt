package com.radiostic.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.io.File

fun getFontFamily(fontName: String, customFilePath: String? = null): FontFamily {
    if ((fontName.equals("Custom", ignoreCase = true) || fontName.startsWith("Custom:")) && !customFilePath.isNullOrBlank()) {
        try {
            val file = File(customFilePath)
            if (file.exists() && file.length() > 0) {
                return FontFamily(Font(file))
            }
        } catch (e: Throwable) {
            // Fallback if custom font fails to parse
        }
    }

    return when (fontName.lowercase()) {
        "serif" -> FontFamily.Serif
        else -> FontFamily.Default // "System Font"
    }
}

fun createRadiosticTypography(
    fontFamily: FontFamily,
    textSizeSetting: String = "Default",
    boldTextSetting: Boolean = false
): Typography {
    val scale = when (textSizeSetting.lowercase()) {
        "small" -> 0.88f
        "large" -> 1.15f
        "extra large" -> 1.30f
        else -> 1.0f
    }

    fun weight(normal: FontWeight, bold: FontWeight): FontWeight {
        return if (boldTextSetting) bold else normal
    }

    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Bold, FontWeight.ExtraBold),
            fontSize = (57 * scale).sp,
            lineHeight = (64 * scale).sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Bold, FontWeight.ExtraBold),
            fontSize = (45 * scale).sp,
            lineHeight = (52 * scale).sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Bold, FontWeight.ExtraBold),
            fontSize = (36 * scale).sp,
            lineHeight = (44 * scale).sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Bold, FontWeight.ExtraBold),
            fontSize = (32 * scale).sp,
            lineHeight = (40 * scale).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Bold, FontWeight.ExtraBold),
            fontSize = (28 * scale).sp,
            lineHeight = (36 * scale).sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Bold, FontWeight.ExtraBold),
            fontSize = (24 * scale).sp,
            lineHeight = (32 * scale).sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Bold, FontWeight.ExtraBold),
            fontSize = (22 * scale).sp,
            lineHeight = (28 * scale).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Medium, FontWeight.Bold),
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Medium, FontWeight.Bold),
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Normal, FontWeight.SemiBold),
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Normal, FontWeight.SemiBold),
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Normal, FontWeight.SemiBold),
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Bold, FontWeight.ExtraBold),
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.SemiBold, FontWeight.Bold),
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight(FontWeight.Medium, FontWeight.Bold),
            fontSize = (11 * scale).sp,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.5.sp
        )
    )
}
