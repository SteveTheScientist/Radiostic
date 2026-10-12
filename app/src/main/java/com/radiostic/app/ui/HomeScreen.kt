package com.radiostic.app.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.radiostic.app.data.RadioStation
import com.radiostic.app.ui.components.ConnectedButtonGroup
import com.radiostic.app.ui.components.EditStationDialog
import com.radiostic.app.ui.components.FabMode
import com.radiostic.app.ui.components.PlayerMenuBottomSheet
import com.radiostic.app.ui.components.PowerFab
import com.radiostic.app.ui.components.RadiosticNavigationBar
import com.radiostic.app.ui.components.RadiosticTopAppBar
import com.radiostic.app.ui.components.ShizukuDialog
import com.radiostic.app.ui.components.StackedListItem
import com.radiostic.app.ui.components.getIconVector
import com.radiostic.app.ui.theme.getRadiosticSpring

@Composable
fun HomeScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    val isRadioOn by viewModel.isRadioOn.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedSubTab by viewModel.selectedSubTab.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val showShizukuDialog by viewModel.showShizukuDialog.collectAsState()
    val showRequirementDialog by viewModel.showRequirementDialog.collectAsState()
    val requirementMessage by viewModel.requirementMessage.collectAsState()
    val showPlayerMenu by viewModel.showPlayerMenu.collectAsState()

    val failedOnlineStationIds by viewModel.failedOnlineStationIds.collectAsState()

    val animationStyle by viewModel.animationStyle.collectAsState()
    val cornerRoundness by viewModel.cornerRoundness.collectAsState()
    val density by viewModel.density.collectAsState()

    val offlineStations by viewModel.offlineStations.collectAsState()
    val onlineStations by viewModel.onlineStations.collectAsState()

    val currentPlayingId by viewModel.currentPlayingStationId.collectAsState()
    val currentPlayingOnlineId by viewModel.currentPlayingOnlineStationId.collectAsState()

    var editingStation by remember { mutableStateOf<RadioStation?>(null) }
    var isAddingNewOnline by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.uiMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Scaffold(
            topBar = {
                Column {
                    RadiosticTopAppBar(
                        isEnabled = isRadioOn,
                        onRefreshClick = { viewModel.refreshChannels() }
                    )
                    TabRow(
                        selectedTabIndex = selectedSubTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedSubTab == 0,
                            onClick = { viewModel.selectSubTab(0) },
                            text = {
                                Text(
                                    text = "Online",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedSubTab == 1,
                            onClick = { viewModel.selectSubTab(1) },
                            text = {
                                Text(
                                    text = "Offline",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            },
            bottomBar = {
                val destination = if (selectedTab == 1) "Favorites" else "Home"
                RadiosticNavigationBar(
                    currentDestination = destination,
                    onNavigate = { dest ->
                        when (dest) {
                            "Home" -> viewModel.selectTab(0)
                            "Favorites" -> viewModel.selectTab(1)
                            "Settings" -> viewModel.navigateTo("Settings")
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = Pair(selectedTab, selectedSubTab),
                    transitionSpec = {
                        val springSpecFloat = getRadiosticSpring<Float>(animationStyle)
                        (fadeIn(animationSpec = springSpecFloat)).togetherWith(fadeOut(animationSpec = springSpecFloat))
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "tabContentTransition"
                ) { (_, subTab) ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (subTab == 0) {
                            // SubTab 0: Online Stations
                            if (selectedTab == 0) {
                                item(key = "online_button_group") {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        var isAddPressed by remember { mutableStateOf(false) }
                                        val addScale by animateFloatAsState(
                                            targetValue = if (isAddPressed) 0.95f else 1.0f,
                                            animationSpec = getRadiosticSpring(animationStyle),
                                            label = "addScale"
                                        )

                                        Button(
                                            onClick = { isAddingNewOnline = true },
                                            shape = RoundedCornerShape(28.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                                .scale(addScale)
                                                .pointerInput(Unit) {
                                                    detectTapGestures(
                                                        onPress = {
                                                            isAddPressed = true
                                                            tryAwaitRelease()
                                                            isAddPressed = false
                                                        }
                                                    )
                                                }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Add Stations",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }

                            if (onlineStations.isEmpty()) {
                                item(key = "online_empty") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No Stations Found.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                itemsIndexed(
                                    items = onlineStations,
                                    key = { _, station -> "online_${station.id}" }
                                ) { index, station ->
                                    val isFailed = failedOnlineStationIds.contains(station.id)

                                    StackedListItem(
                                        station = station,
                                        index = index,
                                        totalCount = onlineStations.size,
                                        isPlaying = station.id == currentPlayingOnlineId,
                                        isEnabled = true,
                                        isFailed = isFailed,
                                        selectedTab = selectedTab,
                                        cornerRoundness = cornerRoundness,
                                        density = density,
                                        onItemClick = { viewModel.playOnlineStation(station) },
                                        onEditClick = { editingStation = station },
                                        onSwipeToFavorite = { isFav -> viewModel.setStationFavorite(station, isFav) },
                                        onSwipeToDelete = { viewModel.deleteStation(station) }
                                    )
                                }
                            }
                        } else {
                            // SubTab 1: Offline Stations
                            if (selectedTab == 0) {
                                item(key = "offline_button_group") {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        ConnectedButtonGroup(
                                            isEnabled = isRadioOn,
                                            isScanning = isScanning,
                                            onScanClick = { viewModel.scanStations() }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }

                            if (offlineStations.isEmpty()) {
                                item(key = "offline_empty") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No Stations Found.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                itemsIndexed(
                                    items = offlineStations,
                                    key = { _, station -> "offline_${station.id}" }
                                ) { index, station ->
                                    StackedListItem(
                                        station = station,
                                        index = index,
                                        totalCount = offlineStations.size,
                                        isPlaying = isRadioOn && station.id == currentPlayingId,
                                        isEnabled = isRadioOn,
                                        selectedTab = selectedTab,
                                        allowSwipeWhenDisabled = true,
                                        cornerRoundness = cornerRoundness,
                                        density = density,
                                        onItemClick = { viewModel.selectPlayingStation(station) },
                                        onEditClick = { editingStation = station },
                                        onSwipeToFavorite = { isFav -> viewModel.setStationFavorite(station, isFav) },
                                        onSwipeToDelete = { viewModel.deleteStation(station) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Mini Player Bar (Floating above navigation bar)
                val isOfflineActive = isRadioOn && currentPlayingId != -1L
                val isOnlineActive = currentPlayingOnlineId != -1L
                val isAnyPlaying = isOfflineActive || isOnlineActive

                val playingOfflineStation = offlineStations.find { it.id == currentPlayingId }
                val playingOnlineStation = onlineStations.find { it.id == currentPlayingOnlineId }
                val activeStation = if (isOfflineActive) playingOfflineStation else playingOnlineStation

                if (isAnyPlaying && activeStation != null) {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                if (isOfflineActive) {
                                    viewModel.toggleRadioPower()
                                } else {
                                    viewModel.stopOnlinePlayback()
                                }
                                true
                            } else {
                                false
                            }
                        }
                    )

                    val startPadding = 16.dp
                    val endPadding = if (selectedSubTab == 1) 88.dp else 16.dp

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {},
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = startPadding, end = endPadding, bottom = 16.dp)
                    ) {
                        Card(
                            onClick = { viewModel.openPlayerMenu() },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getIconVector(activeStation.iconName),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeStation.name.ifBlank { "Station" },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (activeStation.isOnline) "Live Online Stream" else activeStation.frequency,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Power FAB
                if (selectedSubTab == 1) {
                    PowerFab(
                        isRadioOn = isRadioOn,
                        fabMode = FabMode.POWER,
                        animationStyle = animationStyle,
                        onClick = {
                            viewModel.toggleRadioPower()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 16.dp, end = 16.dp)
                    )
                }
            }
        }
    }

    if (showPlayerMenu) {
        PlayerMenuBottomSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.closePlayerMenu() }
        )
    }

    if (isAddingNewOnline) {
        val newOnlineStation = remember {
            RadioStation(
                name = "",
                frequency = "Online",
                isFavorite = false,
                iconName = "Podcasts",
                isOnline = true,
                streamUrl = ""
            )
        }
        EditStationDialog(
            station = newOnlineStation,
            isNewOnline = true,
            onDismiss = { isAddingNewOnline = false },
            onSave = { newName, newStreamUrl, newIconName ->
                viewModel.addOnlineStation(newName, newStreamUrl, newIconName)
                isAddingNewOnline = false
            }
        )
    }

    editingStation?.let { station ->
        EditStationDialog(
            station = station,
            onDismiss = { editingStation = null },
            onSave = { newName, newFrequencyOrUrl, newIconName ->
                viewModel.updateStationDetails(station, newName, newFrequencyOrUrl, newIconName)
                editingStation = null
            }
        )
    }

    if (showRequirementDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRequirementDialog() },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text(
                    text = "Cannot Turn On Radio",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = requirementMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissRequirementDialog() }) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    if (showShizukuDialog) {
        ShizukuDialog(
            onDismiss = { viewModel.dismissShizukuDialog() }
        )
    }
}
