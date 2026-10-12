package com.radiostic.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RadiosticNavigationBar(
    currentDestination: String, // "Home", "Favorites", "Settings"
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = navBarPadding + 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Transparent, // Transparent background
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), // Outlined pill shape
            modifier = Modifier.height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FloatingNavItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentDestination == "Home",
                    onClick = { onNavigate("Home") }
                )

                FloatingNavItem(
                    label = "Favorites",
                    icon = Icons.Default.Favorite,
                    isSelected = currentDestination == "Favorites",
                    onClick = { onNavigate("Favorites") }
                )

                FloatingNavItem(
                    label = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = currentDestination == "Settings",
                    onClick = { onNavigate("Settings") }
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                      else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "navPillColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "navContentColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = springSpec,
        label = "navScale"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .scale(scale)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = if (isSelected) 18.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                        expandHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                       shrinkHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Normal, // Non-bold text
                        color = contentColor
                    )
                }
            }
        }
    }
}
