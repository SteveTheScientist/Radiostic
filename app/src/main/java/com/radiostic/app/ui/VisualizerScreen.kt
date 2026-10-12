package com.radiostic.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.radiostic.app.audio.AudioVisualizerHelper
import com.radiostic.app.ui.components.getIconVector
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VisualizerScreen(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val visualizerMode by viewModel.visualizerMode.collectAsState()
    val isRadioOn by viewModel.isRadioOn.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    val currentPlayingOfflineId by viewModel.currentPlayingStationId.collectAsState()
    val currentPlayingOnlineId by viewModel.currentPlayingOnlineStationId.collectAsState()

    val offlineStations by viewModel.offlineStations.collectAsState()
    val onlineStations by viewModel.onlineStations.collectAsState()

    val isOfflinePlaying = isRadioOn && currentPlayingOfflineId != -1L
    val isOnlinePlaying = currentPlayingOnlineId != -1L

    val playingOfflineStation = offlineStations.find { it.id == currentPlayingOfflineId }
    val playingOnlineStation = onlineStations.find { it.id == currentPlayingOnlineId }
    val activeStation = if (isOfflinePlaying) playingOfflineStation else playingOnlineStation

    val visualizerHelper = remember { AudioVisualizerHelper() }
    val fftData by visualizerHelper.fftData.collectAsState()

    var showControls by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        visualizerHelper.start(0)
    }

    DisposableEffect(Unit) {
        onDispose {
            visualizerHelper.stop()
        }
    }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3500)
            showControls = false
        }
    }

    val view = LocalView.current
    val window = remember(view) { (view.context as? android.app.Activity)?.window }

    DisposableEffect(window) {
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            onDispose {
                controller.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            }
        } else {
            onDispose { }
        }
    }

    val modes = remember { listOf("Bar", "Wave", "Circle") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls },
                    onDoubleTap = { onDismiss() }
                )
            },
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Visualizer Canvas
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary
            val containerColor = MaterialTheme.colorScheme.primaryContainer

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                when (visualizerMode.lowercase()) {
                    "wave" -> {
                        // Smooth Waveform Mode
                        val path = Path()
                        val centerY = height / 2f
                        val sliceWidth = width / (fftData.size - 1)

                        path.moveTo(0f, centerY)
                        for (i in fftData.indices) {
                            val x = i * sliceWidth
                            val amplitude = fftData[i] * 180f
                            val y = if (i % 2 == 0) centerY - amplitude else centerY + amplitude
                            if (i == 0) path.moveTo(x, y)
                            else path.quadraticTo((x - sliceWidth / 2), centerY, x, y)
                        }

                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor, tertiaryColor)),
                            style = Stroke(width = 8f)
                        )
                    }

                    "circle" -> {
                        // Radial Spectrum Mode
                        val centerX = width / 2f
                        val centerY = height / 2f
                        val baseRadius = 140f

                        for (i in fftData.indices) {
                            val angle = (i.toFloat() / fftData.size) * 2 * Math.PI
                            val barLength = fftData[i] * 120f
                            val startX = centerX + baseRadius * cos(angle).toFloat()
                            val startY = centerY + baseRadius * sin(angle).toFloat()
                            val endX = centerX + (baseRadius + barLength) * cos(angle).toFloat()
                            val endY = centerY + (baseRadius + barLength) * sin(angle).toFloat()

                            drawLine(
                                color = if (i % 2 == 0) primaryColor else secondaryColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 10f
                            )
                        }
                    }

                    else -> {
                        // Bar Mode (Default Spectrum Bars)
                        val barCount = fftData.size
                        val barWidth = (width / barCount) * 0.65f
                        val gap = (width / barCount) * 0.35f

                        for (i in 0 until barCount) {
                            val barHeight = (fftData[i] * (height * 0.45f)).coerceAtLeast(12f)
                            val left = i * (barWidth + gap) + gap / 2
                            val top = height / 2f - barHeight / 2f

                            drawRoundRect(
                                brush = Brush.verticalGradient(listOf(primaryColor, secondaryColor)),
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                            )
                        }
                    }
                }
            }

            // Top Header & Close Button
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Full Screen Visualizer",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Visualizer",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Bottom Controls Bar & Mode Selector
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Segmented Button Row for Visualizer Style Selector
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        modes.forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = visualizerMode.equals(label, ignoreCase = true),
                                onClick = { viewModel.setVisualizerMode(label) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size)
                            ) {
                                Text(text = label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Minimal Playing Info & Playback Controls Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getIconVector(activeStation?.iconName ?: "Podcasts"),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = activeStation?.name?.ifBlank { "Radio Station" } ?: "No Active Station",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = activeStation?.let { if (it.isOnline) "Live Stream" else it.frequency } ?: "Station",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedIconButton(
                                    onClick = { viewModel.toggleMute() },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Mute Toggle",
                                        tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                FilledIconButton(
                                    onClick = {
                                        if (isOfflinePlaying || isOnlinePlaying) {
                                            if (isOfflinePlaying) viewModel.toggleRadioPower()
                                            else viewModel.stopOnlinePlayback()
                                        } else {
                                            viewModel.toggleRadioPower()
                                        }
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOfflinePlaying || isOnlinePlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
