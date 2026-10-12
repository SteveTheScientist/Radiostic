package com.radiostic.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.radiostic.app.data.RadioStation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StackedListItem(
    station: RadioStation,
    index: Int,
    totalCount: Int,
    isPlaying: Boolean,
    isEnabled: Boolean,
    isFailed: Boolean = false,
    selectedTab: Int = 0, // 0 = Home, 1 = Favorites
    allowSwipeWhenDisabled: Boolean = false,
    cornerRoundness: String = "Default",
    density: String = "Comfortable",
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onSwipeToFavorite: ((Boolean) -> Unit)? = null,
    onSwipeToDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (outerCorner, innerCorner) = when (cornerRoundness.lowercase()) {
        "sharp" -> Pair(12.dp, 4.dp)
        "extra round" -> Pair(36.dp, 12.dp)
        else -> Pair(28.dp, 8.dp)
    }

    val itemShape = remember(index, totalCount, cornerRoundness) {
        when {
            totalCount <= 1 -> RoundedCornerShape(outerCorner)
            index == 0 -> RoundedCornerShape(
                topStart = outerCorner,
                topEnd = outerCorner,
                bottomStart = innerCorner,
                bottomEnd = innerCorner
            )
            index == totalCount - 1 -> RoundedCornerShape(
                topStart = innerCorner,
                topEnd = innerCorner,
                bottomStart = outerCorner,
                bottomEnd = outerCorner
            )
            else -> RoundedCornerShape(innerCorner)
        }
    }

    val itemHeight = if (density.equals("Compact", ignoreCase = true)) 56.dp else 72.dp

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.6f),
        label = "itemScale"
    )

    val backgroundColor = when {
        isFailed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        isPlaying && isEnabled -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val alpha = if (isEnabled) 1.0f else 0.38f

    val borderModifier = when {
        isFailed -> Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            shape = itemShape
        )
        isPlaying && isEnabled -> Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            shape = itemShape
        )
        else -> Modifier
    }

    if ((onSwipeToFavorite != null || onSwipeToDelete != null) && (isEnabled || allowSwipeWhenDisabled)) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        if (selectedTab == 0) {
                            onSwipeToFavorite?.invoke(true)
                        } else {
                            onSwipeToDelete?.invoke()
                        }
                        false
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        if (selectedTab == 0) {
                            onSwipeToDelete?.invoke()
                        } else {
                            onSwipeToFavorite?.invoke(false)
                        }
                        false
                    }
                    SwipeToDismissBoxValue.Settled -> false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val color = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> if (selectedTab == 0) Color(0xFFE53935) else MaterialTheme.colorScheme.error
                    SwipeToDismissBoxValue.EndToStart -> if (selectedTab == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    else -> Color.Transparent
                }
                val icon = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> if (selectedTab == 0) Icons.Default.Favorite else Icons.Default.Delete
                    SwipeToDismissBoxValue.EndToStart -> if (selectedTab == 0) Icons.Default.Delete else Icons.Default.FavoriteBorder
                    else -> null
                }
                val alignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.Center
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(itemShape)
                        .background(color.copy(alpha = 0.25f))
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    if (icon != null && direction != SwipeToDismissBoxValue.Settled) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            modifier = modifier
        ) {
            ListItemContent(
                station = station,
                itemShape = itemShape,
                itemHeight = itemHeight,
                scale = scale,
                backgroundColor = backgroundColor,
                alpha = alpha,
                borderModifier = borderModifier,
                isPlaying = isPlaying,
                isEnabled = isEnabled,
                isFailed = isFailed,
                onItemClick = onItemClick,
                onEditClick = onEditClick,
                onPressChange = { isPressed = it }
            )
        }
    } else {
        ListItemContent(
            station = station,
            itemShape = itemShape,
            itemHeight = itemHeight,
            scale = scale,
            backgroundColor = backgroundColor,
            alpha = alpha,
            borderModifier = borderModifier,
            isPlaying = isPlaying,
            isEnabled = isEnabled,
            isFailed = isFailed,
            onItemClick = onItemClick,
            onEditClick = onEditClick,
            onPressChange = { isPressed = it },
            modifier = modifier
        )
    }
}

@Composable
private fun ListItemContent(
    station: RadioStation,
    itemShape: RoundedCornerShape,
    itemHeight: androidx.compose.ui.unit.Dp,
    scale: Float,
    backgroundColor: Color,
    alpha: Float,
    borderModifier: Modifier,
    isPlaying: Boolean,
    isEnabled: Boolean,
    isFailed: Boolean = false,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onPressChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryTextColor = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = if (isFailed) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(itemHeight)
            .scale(scale)
            .clip(itemShape)
            .background(backgroundColor.copy(alpha = alpha))
            .then(borderModifier)
            .pointerInput(isEnabled) {
                detectTapGestures(
                    onPress = {
                        if (isEnabled) {
                            onPressChange(true)
                            tryAwaitRelease()
                            onPressChange(false)
                        }
                    },
                    onTap = {
                        if (isEnabled) {
                            onItemClick()
                        }
                    }
                )
            }
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isFailed -> MaterialTheme.colorScheme.errorContainer
                            isPlaying && isEnabled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconVector(station.iconName),
                    contentDescription = null,
                    tint = when {
                        isFailed -> MaterialTheme.colorScheme.onErrorContainer
                        isPlaying && isEnabled -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = alpha)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = station.name.ifBlank { "Station" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = primaryTextColor.copy(alpha = alpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (station.isOnline) (station.streamUrl ?: "Online") else station.frequency,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryTextColor.copy(alpha = alpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = { if (isEnabled) onEditClick() },
                enabled = isEnabled,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit station",
                    tint = secondaryTextColor.copy(alpha = alpha),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
