package com.radiostic.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.radiostic.app.data.RadioStation

fun getIconVector(iconName: String): ImageVector {
    return when (iconName) {
        "Radio" -> Icons.Default.Radio
        "MusicNote" -> Icons.Default.MusicNote
        "GraphicEq" -> Icons.Default.GraphicEq
        "VolumeUp" -> Icons.AutoMirrored.Filled.VolumeUp
        "SignalCellularAlt" -> Icons.Default.SignalCellularAlt
        "Headphones" -> Icons.Default.Headphones
        "Mic" -> Icons.Default.Mic
        "Speaker" -> Icons.Default.Speaker
        "Waves" -> Icons.Default.Waves
        "LibraryMusic" -> Icons.Default.LibraryMusic
        "Explore" -> Icons.Default.Explore
        else -> Icons.Default.Podcasts
    }
}

@Composable
fun EditStationDialog(
    station: RadioStation,
    isNewOnline: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (newName: String, newFrequencyOrUrl: String, newIconName: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var nameText by remember { mutableStateOf(if (isNewOnline && station.name.isBlank()) "Online Station" else station.name) }
    var frequencyText by remember { mutableStateOf(if (station.isOnline || isNewOnline) (station.streamUrl ?: "") else station.frequency) }
    var selectedIconName by remember { mutableStateOf(station.iconName) }

    val availableIcons = listOf(
        "Podcasts" to Icons.Default.Podcasts,
        "Radio" to Icons.Default.Radio,
        "MusicNote" to Icons.Default.MusicNote,
        "Headphones" to Icons.Default.Headphones,
        "Mic" to Icons.Default.Mic,
        "Speaker" to Icons.Default.Speaker,
        "Waves" to Icons.Default.Waves,
        "LibraryMusic" to Icons.Default.LibraryMusic,
        "GraphicEq" to Icons.Default.GraphicEq,
        "Explore" to Icons.Default.Explore,
        "VolumeUp" to Icons.AutoMirrored.Filled.VolumeUp,
        "SignalCellularAlt" to Icons.Default.SignalCellularAlt
    )

    val halfIndex = (availableIcons.size + 1) / 2
    val firstRowIcons = availableIcons.take(halfIndex)
    val secondRowIcons = availableIcons.drop(halfIndex)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = if (isNewOnline) "Add Online Station" else if (station.isOnline) "Edit Online Station" else "Edit Station",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Station Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = frequencyText,
                    onValueChange = { frequencyText = it },
                    label = { Text(if (station.isOnline || isNewOnline) "Stream URL (http:// or https://)" else "Frequency (e.g. 91.8 MHz)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Station Icon",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        firstRowIcons.forEach { (iconName, vector) ->
                            val isSelected = selectedIconName == iconName
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedIconName = iconName },
                                shape = RoundedCornerShape(8.dp),
                                label = {
                                    Text(
                                        text = iconName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = vector,
                                        contentDescription = iconName,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        secondRowIcons.forEach { (iconName, vector) ->
                            val isSelected = selectedIconName == iconName
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedIconName = iconName },
                                shape = RoundedCornerShape(8.dp),
                                label = {
                                    Text(
                                        text = iconName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = vector,
                                        contentDescription = iconName,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(nameText.trim(), frequencyText.trim(), selectedIconName)
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Normal
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
