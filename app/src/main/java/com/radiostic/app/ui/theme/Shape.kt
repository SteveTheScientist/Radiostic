package com.radiostic.app.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun createRadiosticShapes(cornerRoundness: String): Shapes {
    return when (cornerRoundness.lowercase()) {
        "sharp" -> Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small = RoundedCornerShape(6.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(12.dp),
            extraLarge = RoundedCornerShape(16.dp)
        )
        "extra round" -> Shapes(
            extraSmall = RoundedCornerShape(12.dp),
            small = RoundedCornerShape(18.dp),
            medium = RoundedCornerShape(24.dp),
            large = RoundedCornerShape(28.dp),
            extraLarge = RoundedCornerShape(36.dp)
        )
        else -> Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(28.dp)
        )
    }
}

fun <T> getRadiosticSpring(animationStyle: String): FiniteAnimationSpec<T> {
    return when (animationStyle.lowercase()) {
        "off" -> tween(durationMillis = 0)
        "calm" -> spring(stiffness = 200f, dampingRatio = 0.95f)
        else -> spring(stiffness = 300f, dampingRatio = 0.65f) // Expressive
    }
}

val RadiosticShapes = createRadiosticShapes("Default")
