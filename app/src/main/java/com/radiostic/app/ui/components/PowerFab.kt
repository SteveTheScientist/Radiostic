package com.radiostic.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.radiostic.app.ui.theme.getRadiosticSpring

enum class FabMode {
    POWER,
    PLUS
}

@Composable
fun PowerFab(
    isRadioOn: Boolean,
    fabMode: FabMode = FabMode.POWER,
    animationStyle: String = "Expressive",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val springSpecFloat = remember(animationStyle) { getRadiosticSpring<Float>(animationStyle) }
    val springSpecColor = remember(animationStyle) { getRadiosticSpring<androidx.compose.ui.graphics.Color>(animationStyle) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = springSpecFloat,
        label = "fabScale"
    )

    val fabShape = RoundedCornerShape(16.dp)

    val containerColor by animateColorAsState(
        targetValue = if (isRadioOn) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = springSpecColor,
        label = "fabBg"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isRadioOn) MaterialTheme.colorScheme.onPrimary
                      else MaterialTheme.colorScheme.onPrimaryContainer,
        animationSpec = springSpecColor,
        label = "fabIcon"
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .scale(scale)
            .shadow(elevation = 6.dp, shape = fabShape) // Level 3 shadow (~6dp)
            .clip(fabShape)
            .background(containerColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = fabMode,
            transitionSpec = {
                (fadeIn(animationSpec = springSpecFloat) + scaleIn(animationSpec = springSpecFloat, initialScale = 0.5f))
                    .togetherWith(fadeOut(animationSpec = springSpecFloat) + scaleOut(animationSpec = springSpecFloat, targetScale = 0.5f))
            },
            label = "fabIconTransition"
        ) { mode ->
            if (mode == FabMode.PLUS) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Online Station",
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power Radio On/Off",
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
