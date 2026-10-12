package com.radiostic.app.ui

import android.app.Application
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.radiostic.app.data.AppDatabase
import com.radiostic.app.data.PreferencesRepository
import com.radiostic.app.data.RadioStation
import com.radiostic.app.fm.FmEngine
import com.radiostic.app.fm.Freq
import com.radiostic.app.fm.RadioPlaybackService
import com.radiostic.app.shizuku.ShizukuHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val stationDao = db.radioStationDao()
    private val prefs = PreferencesRepository(application)

    private val _isRadioOn = MutableStateFlow(false)
    val isRadioOn: StateFlow<Boolean> = _isRadioOn.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _audioRouteState = MutableStateFlow(0) // 0 = Headphones, 1 = Speaker, 2 = Muted
    val audioRouteState: StateFlow<Int> = _audioRouteState.asStateFlow()

    private val _showPlayerMenu = MutableStateFlow(false)
    val showPlayerMenu: StateFlow<Boolean> = _showPlayerMenu.asStateFlow()

    private val _showVisualizer = MutableStateFlow(false)
    val showVisualizer: StateFlow<Boolean> = _showVisualizer.asStateFlow()

    private val _visualizerMode = MutableStateFlow(
        if (prefs.visualizerMode.equals("Particles", ignoreCase = true)) "Bar" else prefs.visualizerMode
    )
    val visualizerMode: StateFlow<String> = _visualizerMode.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0 = Home, 1 = Favorites
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedSubTab = MutableStateFlow(0) // 0 = Online Stations, 1 = Offline Stations
    val selectedSubTab: StateFlow<Int> = _selectedSubTab.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _showShizukuDialog = MutableStateFlow(false)
    val showShizukuDialog: StateFlow<Boolean> = _showShizukuDialog.asStateFlow()

    private val _showRequirementDialog = MutableStateFlow(false)
    val showRequirementDialog: StateFlow<Boolean> = _showRequirementDialog.asStateFlow()

    private val _requirementMessage = MutableStateFlow("")
    val requirementMessage: StateFlow<String> = _requirementMessage.asStateFlow()

    private val _uiMessage = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    private val _currentPlayingStationId = MutableStateFlow(prefs.currentPlayingId)
    val currentPlayingStationId: StateFlow<Long> = _currentPlayingStationId.asStateFlow()

    private val _currentPlayingOnlineStationId = MutableStateFlow(-1L)
    val currentPlayingOnlineStationId: StateFlow<Long> = _currentPlayingOnlineStationId.asStateFlow()

    private val _failedOnlineStationIds = MutableStateFlow<Set<Long>>(emptySet())
    val failedOnlineStationIds: StateFlow<Set<Long>> = _failedOnlineStationIds.asStateFlow()

    private var exoPlayer: ExoPlayer? = null

    private val _selectedFont = MutableStateFlow(prefs.selectedFont)
    val selectedFont: StateFlow<String> = _selectedFont.asStateFlow()

    data class CustomFontItem(val name: String, val filePath: String)

    private val _customFonts = MutableStateFlow<List<CustomFontItem>>(loadCustomFontsFromPrefs())
    val customFonts: StateFlow<List<CustomFontItem>> = _customFonts.asStateFlow()

    private val _customFontFilePath = MutableStateFlow(prefs.customFontFilePath)
    val customFontFilePath: StateFlow<String?> = _customFontFilePath.asStateFlow()

    private val _customFontFileName = MutableStateFlow(prefs.customFontFileName)
    val customFontFileName: StateFlow<String?> = _customFontFileName.asStateFlow()

    private fun loadCustomFontsFromPrefs(): List<CustomFontItem> {
        val json = prefs.customFontListJson
        if (json.isNotBlank()) {
            try {
                val list = mutableListOf<CustomFontItem>()
                val array = org.json.JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val name = obj.getString("name")
                    val path = obj.getString("filePath")
                    if (File(path).exists()) {
                        list.add(CustomFontItem(name, path))
                    }
                }
                return list
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
        val singlePath = prefs.customFontFilePath
        val singleName = prefs.customFontFileName
        if (!singlePath.isNullOrBlank() && !singleName.isNullOrBlank() && File(singlePath).exists()) {
            return listOf(CustomFontItem(singleName, singlePath))
        }
        return emptyList()
    }

    private fun saveCustomFontsToPrefs(list: List<CustomFontItem>) {
        val array = org.json.JSONArray()
        for (item in list) {
            val obj = org.json.JSONObject()
            obj.put("name", item.name)
            obj.put("filePath", item.filePath)
            array.put(obj)
        }
        prefs.customFontListJson = array.toString()
    }

    private val _useDynamicColor = MutableStateFlow(prefs.useDynamicColor)
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _animationStyle = MutableStateFlow(prefs.animationStyle)
    val animationStyle: StateFlow<String> = _animationStyle.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.hapticsEnabled)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _cornerRoundness = MutableStateFlow(prefs.cornerRoundness)
    val cornerRoundness: StateFlow<String> = _cornerRoundness.asStateFlow()

    private val _density = MutableStateFlow(prefs.density)
    val density: StateFlow<String> = _density.asStateFlow()

    private val _textSize = MutableStateFlow(prefs.textSize)
    val textSize: StateFlow<String> = _textSize.asStateFlow()

    private val _boldText = MutableStateFlow(prefs.boldText)
    val boldText: StateFlow<Boolean> = _boldText.asStateFlow()

    private val _currentScreen = MutableStateFlow("Home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private var pendingPowerOn = false

    val offlineStations: StateFlow<List<RadioStation>> = combine(
        _selectedTab,
        stationDao.getOfflineStations(),
        stationDao.getOfflineFavoriteStations()
    ) { tab, all, favs ->
        if (tab == 1) favs else all
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val onlineStations: StateFlow<List<RadioStation>> = combine(
        _selectedTab,
        stationDao.getOnlineStations(),
        stationDao.getOnlineFavoriteStations()
    ) { tab, all, favs ->
        if (tab == 1) favs else all
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allOfflineStationsOnce: StateFlow<List<RadioStation>> = stationDao.getOfflineStations().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allOnlineStationsOnce: StateFlow<List<RadioStation>> = stationDao.getOnlineStations().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isPlayingStationFavorite: StateFlow<Boolean> = combine(
        currentPlayingStationId,
        allOfflineStationsOnce
    ) { currentId, stations ->
        stations.find { it.id == currentId }?.isFavorite ?: false
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isPlayingOnlineStationFavorite: StateFlow<Boolean> = combine(
        currentPlayingOnlineStationId,
        allOnlineStationsOnce
    ) { currentId, stations ->
        stations.find { it.id == currentId }?.isFavorite ?: false
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        ShizukuHelper.init()

        RadioPlaybackService.onStopActionCallback = {
            if (_isRadioOn.value) {
                executePowerOff()
            }
            if (_currentPlayingOnlineStationId.value != -1L) {
                stopOnlinePlayback()
            }
        }

        viewModelScope.launch {
            ShizukuHelper.isPermissionGranted.collect { granted ->
                if (granted) {
                    performStartupSync()
                    if (pendingPowerOn) {
                        pendingPowerOn = false
                        executePowerOn()
                    }
                } else {
                    if (_isRadioOn.value) {
                        handleFmError("Shizuku permission revoked.")
                    }
                }
            }
        }

        viewModelScope.launch {
            ShizukuHelper.isShizukuAvailable.collect { available ->
                if (!available && _isRadioOn.value) {
                    handleFmError("Lost the FM connection... Make sure Shizuku is still running.")
                }
            }
        }

        viewModelScope.launch {
            FmEngine.fmEvents.rdsFlow.collect { event ->
                val name = event.stationName?.trim()
                if (!name.isNullOrBlank() && name != "Station") {
                    val currentId = _currentPlayingStationId.value
                    val stations = allOfflineStationsOnce.value
                    val current = stations.find { it.id == currentId }
                    if (current != null && (current.name == "Station" || current.name.startsWith("Station "))) {
                        val parsedKhz = Freq.parseKhz(current.frequency)
                        if (parsedKhz == event.kHz) {
                            stationDao.updateStation(current.copy(name = name))
                            updateForegroundServiceNotification(name, current.frequency)
                        }
                    }
                }
            }
        }
    }

    private fun updateForegroundServiceNotification(name: String, frequency: String) {
        if (_isRadioOn.value) {
            RadioPlaybackService.startService(getApplication(), name, frequency)
        }
    }

    private fun performStartupSync() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (FmEngine.connect() && FmEngine.isOn()) {
                    _isRadioOn.value = true
                    FmEngine.setListener(FmEngine.fmEvents)
                    val stations = stationDao.getAllOnce()
                    val playing = stations.find { it.id == _currentPlayingStationId.value } ?: stations.firstOrNull()
                    updateForegroundServiceNotification(playing?.name ?: "Station", playing?.frequency ?: "88.0 MHz")
                    applyAudioRoute(_audioRouteState.value)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _isRadioOn.value = false
                FmEngine.disconnect()
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun setFont(fontName: String, customPath: String? = null) {
        _selectedFont.value = fontName
        prefs.selectedFont = fontName
        if (customPath != null) {
            _customFontFilePath.value = customPath
            prefs.customFontFilePath = customPath
        }
    }

    fun selectCustomFontItem(item: CustomFontItem) {
        _customFontFilePath.value = item.filePath
        _customFontFileName.value = item.name
        _selectedFont.value = "Custom:${item.name}"

        prefs.customFontFilePath = item.filePath
        prefs.customFontFileName = item.name
        prefs.selectedFont = "Custom:${item.name}"
    }

    fun importCustomFonts(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val currentList = _customFonts.value.toMutableList()
            var importedCount = 0

            for (uri in uris) {
                try {
                    var rawFileName = "custom_font.ttf"
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1 && cursor.moveToFirst()) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) {
                                rawFileName = name
                            }
                        }
                    }

                    val displayNameWithoutExtension = rawFileName.substringBeforeLast('.')
                    val newFontFileName = "custom_font_${System.currentTimeMillis()}_${(0..999).random()}.ttf"
                    val destFile = File(context.filesDir, newFontFileName)

                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        FileOutputStream(destFile).use { outputStream ->
                            inputStream.use { input ->
                                input.copyTo(outputStream)
                            }
                        }

                        val valid = try {
                            val tf = android.graphics.Typeface.createFromFile(destFile)
                            tf != null
                        } catch (e: Exception) {
                            false
                        }

                        if (valid) {
                            val newItem = CustomFontItem(displayNameWithoutExtension, destFile.absolutePath)
                            currentList.removeAll { it.name.equals(displayNameWithoutExtension, ignoreCase = true) }
                            currentList.add(newItem)
                            importedCount++
                        } else {
                            destFile.delete()
                        }
                    }
                } catch (e: Exception) {
                    // Skip
                }
            }

            withContext(Dispatchers.Main) {
                _customFonts.value = currentList
                saveCustomFontsToPrefs(currentList)

                val latestItem = currentList.lastOrNull()
                if (latestItem != null) {
                    selectCustomFontItem(latestItem)
                }

                if (importedCount > 0) {
                    _uiMessage.tryEmit("Imported $importedCount font(s).")
                } else {
                    _uiMessage.tryEmit("Failed to import font(s).")
                }
            }
        }
    }

    fun deleteCustomFontItem(item: CustomFontItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(item.filePath)
            if (file.exists()) {
                file.delete()
            }
            val currentList = _customFonts.value.toMutableList()
            currentList.remove(item)

            withContext(Dispatchers.Main) {
                _customFonts.value = currentList
                saveCustomFontsToPrefs(currentList)

                if (_customFontFilePath.value == item.filePath) {
                    val fallback = currentList.lastOrNull()
                    if (fallback != null) {
                        selectCustomFontItem(fallback)
                    } else {
                        _customFontFilePath.value = null
                        _customFontFileName.value = null
                        _selectedFont.value = "System Font"

                        prefs.customFontFilePath = null
                        prefs.customFontFileName = null
                        prefs.selectedFont = "System Font"
                    }
                }
            }
        }
    }

    fun setUseDynamicColor(useDynamic: Boolean) {
        _useDynamicColor.value = useDynamic
        prefs.useDynamicColor = useDynamic
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.themeMode = mode
    }

    fun setAnimationStyle(style: String) {
        _animationStyle.value = style
        prefs.animationStyle = style
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
        prefs.hapticsEnabled = enabled
    }

    fun setCornerRoundness(roundness: String) {
        _cornerRoundness.value = roundness
        prefs.cornerRoundness = roundness
    }

    fun setDensity(densityValue: String) {
        _density.value = densityValue
        prefs.density = densityValue
    }

    fun setTextSize(size: String) {
        _textSize.value = size
        prefs.textSize = size
    }

    fun setBoldText(bold: Boolean) {
        _boldText.value = bold
        prefs.boldText = bold
    }

    fun refreshChannels() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiMessage.tryEmit("Refreshing channels...")

            // Test online channels
            val onlineList = stationDao.getOnlineOnce()
            val failedIds = mutableSetOf<Long>()

            for (station in onlineList) {
                val urlStr = station.streamUrl
                if (urlStr.isNullOrBlank()) {
                    failedIds.add(station.id)
                    continue
                }
                try {
                    val url = URL(urlStr)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    conn.requestMethod = "GET"
                    conn.connect()
                    val code = conn.responseCode
                    conn.disconnect()
                    if (code !in 200..399) {
                        failedIds.add(station.id)
                    }
                } catch (e: Exception) {
                    failedIds.add(station.id)
                }
            }

            _failedOnlineStationIds.value = failedIds

            // Refresh offline channels
            if (_isRadioOn.value) {
                scanStations()
            }

            withContext(Dispatchers.Main) {
                _uiMessage.tryEmit("Channels refreshed.")
            }
        }
    }

    fun toggleRadioPower() {
        ShizukuHelper.checkShizukuStatus()
        if (_isRadioOn.value) {
            executePowerOff()
        } else {
            val isAvailable = ShizukuHelper.isShizukuAvailable.value
            val isGranted = ShizukuHelper.isPermissionGranted.value

            if (!isAvailable) {
                _requirementMessage.value = "Shizuku is not running. Please start Shizuku and plug in wired earphones as an antenna."
                _showRequirementDialog.value = true
                return
            }

            if (!isGranted) {
                pendingPowerOn = true
                ShizukuHelper.requestPermission()
                return
            }

            executePowerOn()
        }
    }

    private fun executePowerOn() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!FmEngine.connect()) {
                    handleFmError("Lost the FM connection... Make sure Shizuku is still running.")
                    return@launch
                }

                if (!FmEngine.isHeadsetPlugged()) {
                    withContext(Dispatchers.Main) {
                        _requirementMessage.value = "Earphones are not plugged in. Wired earphones act as the FM radio antenna."
                        _showRequirementDialog.value = true
                    }
                    FmEngine.disconnect()
                    return@launch
                }

                stopOnlinePlayback()

                FmEngine.setListener(FmEngine.fmEvents)

                if (!FmEngine.on()) {
                    handleFmError("Failed to turn on FM chip.")
                    return@launch
                }

                FmEngine.enableRDS()

                val stations = stationDao.getOfflineOnce()
                val lastId = _currentPlayingStationId.value
                val playing = stations.find { it.id == lastId } ?: stations.firstOrNull()

                if (playing != null) {
                    val kHz = Freq.parseKhz(playing.frequency)
                    if (kHz != null) {
                        FmEngine.tune(kHz)
                    }
                }

                _isRadioOn.value = true

                applyAudioRoute(_audioRouteState.value)

                val channelName = playing?.name?.ifBlank { "Station" } ?: "Station"
                val channelFreq = playing?.frequency ?: "88.0 MHz"
                withContext(Dispatchers.Main) {
                    RadioPlaybackService.startService(
                        getApplication(),
                        channelName,
                        channelFreq,
                        isOnline = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                handleFmError("Lost the FM connection... Make sure Shizuku is still running.")
            }
        }
    }

    private fun executePowerOff() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                FmEngine.off()
                FmEngine.removeListener(FmEngine.fmEvents)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                // Ignore
            } finally {
                FmEngine.disconnect()
                _isRadioOn.value = false
                _isScanning.value = false
                withContext(Dispatchers.Main) {
                    RadioPlaybackService.stopService(getApplication())
                }
            }
        }
    }

    private suspend fun handleFmError(msg: String) {
        withContext(Dispatchers.Main) {
            _isRadioOn.value = false
            _isScanning.value = false
            _uiMessage.tryEmit(msg)
            RadioPlaybackService.stopService(getApplication())
        }
        FmEngine.disconnect()
    }

    fun dismissRequirementDialog() {
        _showRequirementDialog.value = false
    }

    fun dismissShizukuDialog() {
        _showShizukuDialog.value = false
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun selectSubTab(subTabIndex: Int) {
        _selectedSubTab.value = subTabIndex
    }

    fun openPlayerMenu() {
        _showPlayerMenu.value = true
    }

    fun closePlayerMenu() {
        _showPlayerMenu.value = false
    }

    fun openVisualizer() {
        _showVisualizer.value = true
    }

    fun closeVisualizer() {
        _showVisualizer.value = false
    }

    fun setVisualizerMode(mode: String) {
        _visualizerMode.value = mode
        prefs.visualizerMode = mode
    }

    fun isHeadphonesConnected(): Boolean {
        val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            when (device.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> return true
            }
        }
        return false
    }

    fun toggleAudioRoute() {
        val hasHeadphones = isHeadphonesConnected()
        val current = _audioRouteState.value

        val next = if (hasHeadphones) {
            when (current) {
                0 -> 1 // Headphone -> Speaker
                1 -> 2 // Speaker -> Muted
                else -> 0 // Muted -> Headphone
            }
        } else {
            if (current == 2) 1 else 2 // Toggle Speaker <-> Muted
        }

        _audioRouteState.value = next
        applyAudioRoute(next)
    }

    private fun applyAudioRoute(routeState: Int) {
        val context = getApplication<Application>()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        viewModelScope.launch(Dispatchers.IO) {
            val isSpeaker = (routeState == 1)
            val isMute = (routeState == 2)

            // FM Radio speaker hardware control
            if (_isRadioOn.value) {
                try {
                    FmEngine.setSpeakerOn(isSpeaker)
                    FmEngine.mute(isMute)
                } catch (e: Exception) {
                    // Ignore
                }
            }

            // Online stream audio routing
            withContext(Dispatchers.Main) {
                if (isMute) {
                    exoPlayer?.volume = 0f
                } else {
                    exoPlayer?.volume = 1f
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (isSpeaker) {
                        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                        val speakerDevice = devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                        if (speakerDevice != null) {
                            audioManager.setCommunicationDevice(speakerDevice)
                        } else {
                            @Suppress("DEPRECATION")
                            audioManager.isSpeakerphoneOn = true
                        }
                    } else {
                        audioManager.clearCommunicationDevice()
                        @Suppress("DEPRECATION")
                        audioManager.isSpeakerphoneOn = false
                    }
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.isSpeakerphoneOn = isSpeaker
                }
            }
        }
    }

    fun toggleMute() {
        toggleAudioRoute()
    }

    fun playPreviousStation() {
        if (_isRadioOn.value) {
            val list = offlineStations.value
            if (list.isEmpty()) return
            val currentId = _currentPlayingStationId.value
            val index = list.indexOfFirst { it.id == currentId }
            val nextIndex = if (index <= 0) list.size - 1 else index - 1
            selectPlayingStation(list[nextIndex])
        } else if (_currentPlayingOnlineStationId.value != -1L) {
            val list = onlineStations.value
            if (list.isEmpty()) return
            val currentId = _currentPlayingOnlineStationId.value
            val index = list.indexOfFirst { it.id == currentId }
            val nextIndex = if (index <= 0) list.size - 1 else index - 1
            playOnlineStation(list[nextIndex])
        }
    }

    fun playNextStation() {
        if (_isRadioOn.value) {
            val list = offlineStations.value
            if (list.isEmpty()) return
            val currentId = _currentPlayingStationId.value
            val index = list.indexOfFirst { it.id == currentId }
            val nextIndex = if (index < 0 || index >= list.size - 1) 0 else index + 1
            selectPlayingStation(list[nextIndex])
        } else if (_currentPlayingOnlineStationId.value != -1L) {
            val list = onlineStations.value
            if (list.isEmpty()) return
            val currentId = _currentPlayingOnlineStationId.value
            val index = list.indexOfFirst { it.id == currentId }
            val nextIndex = if (index < 0 || index >= list.size - 1) 0 else index + 1
            playOnlineStation(list[nextIndex])
        }
    }

    fun selectPlayingStation(station: RadioStation) {
        val kHz = Freq.parseKhz(station.frequency)
        if (kHz == null) {
            _uiMessage.tryEmit("Invalid station frequency: ${station.frequency}")
            return
        }

        if (_currentPlayingOnlineStationId.value != -1L) {
            _currentPlayingOnlineStationId.value = -1L
            viewModelScope.launch(Dispatchers.Main) {
                exoPlayer?.stop()
                exoPlayer?.clearMediaItems()
            }
        }

        _currentPlayingStationId.value = station.id
        prefs.currentPlayingId = station.id

        val channelName = station.name.ifBlank { "Station" }
        if (!_isRadioOn.value) {
            toggleRadioPower()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    FmEngine.tune(kHz)
                    applyAudioRoute(_audioRouteState.value)
                    withContext(Dispatchers.Main) {
                        RadioPlaybackService.startService(getApplication(), channelName, station.frequency, isOnline = false)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    handleFmError("Lost the FM connection... Make sure Shizuku is still running.")
                }
            }
        }
    }

    fun playOnlineStation(station: RadioStation) {
        val url = station.streamUrl
        if (url.isNullOrBlank() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            _uiMessage.tryEmit("Invalid stream URL: '$url'")
            _failedOnlineStationIds.value = _failedOnlineStationIds.value + station.id
            return
        }

        if (_currentPlayingOnlineStationId.value == station.id) {
            stopOnlinePlayback()
            return
        }

        if (_isRadioOn.value) {
            executePowerOff()
        }

        _currentPlayingOnlineStationId.value = station.id

        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (exoPlayer == null) {
                    exoPlayer = ExoPlayer.Builder(getApplication()).build().apply {
                        addListener(object : Player.Listener {
                            override fun onPlayerError(error: PlaybackException) {
                                val currentOnlineId = _currentPlayingOnlineStationId.value
                                if (currentOnlineId != -1L) {
                                    _failedOnlineStationIds.value = _failedOnlineStationIds.value + currentOnlineId
                                }
                                _uiMessage.tryEmit("Playback error: ${error.localizedMessage ?: "Failed to connect to stream"}")
                                stopOnlinePlayback()
                            }

                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                if (isPlaying) {
                                    val currentOnlineId = _currentPlayingOnlineStationId.value
                                    if (currentOnlineId != -1L) {
                                        _failedOnlineStationIds.value = _failedOnlineStationIds.value - currentOnlineId
                                    }
                                }
                            }
                        })
                    }
                }

                val player = exoPlayer!!
                player.stop()
                player.clearMediaItems()
                player.setMediaItem(MediaItem.fromUri(url))
                player.prepare()
                player.play()

                applyAudioRoute(_audioRouteState.value)

                val channelName = station.name.ifBlank { "Online Station" }
                RadioPlaybackService.startService(
                    getApplication(),
                    channelName,
                    station.streamUrl ?: "",
                    isOnline = true
                )
            } catch (e: Exception) {
                _failedOnlineStationIds.value = _failedOnlineStationIds.value + station.id
                _uiMessage.tryEmit("Error starting online stream: ${e.localizedMessage}")
                stopOnlinePlayback()
            }
        }
    }

    fun stopOnlinePlayback() {
        viewModelScope.launch(Dispatchers.Main) {
            _currentPlayingOnlineStationId.value = -1L
            exoPlayer?.stop()
            exoPlayer?.clearMediaItems()
            if (!_isRadioOn.value) {
                RadioPlaybackService.stopService(getApplication())
            }
        }
    }

    fun scanStations() {
        if (!_isRadioOn.value || _isScanning.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true
            try {
                FmEngine.scanAsync()

                delay(8000)

                var polls = 0
                while (FmEngine.isScanning() && polls < 40) {
                    delay(500)
                    polls++
                }
                delay(500)

                val results = FmEngine.getLastScanResult()

                if (results.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _uiMessage.tryEmit("No stations found. Check that your earphones are plugged in.")
                    }
                } else {
                    stationDao.deleteNonFavoriteOfflineStations()
                    val existing = stationDao.getOfflineOnce()
                    val existingKhzSet = existing.mapNotNull { Freq.parseKhz(it.frequency) }.toSet()

                    var currentMaxOrder = (existing.maxOfOrNull { it.displayOrder } ?: 0)
                    val newStations = mutableListOf<RadioStation>()

                    val sortedResults = results.distinct().sorted()
                    for (kHz in sortedResults) {
                        if (!existingKhzSet.contains(kHz)) {
                            currentMaxOrder++
                            newStations.add(
                                RadioStation(
                                    name = "Station",
                                    frequency = Freq.formatMhz(kHz),
                                    isFavorite = false,
                                    iconName = "Podcasts",
                                    displayOrder = currentMaxOrder
                                )
                            )
                        }
                    }

                    if (newStations.isNotEmpty()) {
                        stationDao.insertStations(newStations)
                    }

                    val updatedAll = stationDao.getAllOnce()
                    val lastPlayingId = _currentPlayingStationId.value
                    val stillPlaying = updatedAll.find { it.id == lastPlayingId }
                    val targetStation = stillPlaying ?: updatedAll.firstOrNull()

                    if (targetStation != null) {
                        _currentPlayingStationId.value = targetStation.id
                        prefs.currentPlayingId = targetStation.id
                        val kHz = Freq.parseKhz(targetStation.frequency)
                        if (kHz != null) {
                            FmEngine.tune(kHz)
                            applyAudioRoute(_audioRouteState.value)
                            withContext(Dispatchers.Main) {
                                RadioPlaybackService.startService(getApplication(), targetStation.name, targetStation.frequency)
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                handleFmError("Lost the FM connection... Make sure Shizuku is still running.")
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun toggleCurrentStationFavorite() {
        if (!_isRadioOn.value) {
            _uiMessage.tryEmit("Turn on FM radio first.")
            return
        }
        viewModelScope.launch {
            val currentId = _currentPlayingStationId.value
            val currentStations = allOfflineStationsOnce.value
            val playingStation = currentStations.find { it.id == currentId }

            if (playingStation == null) {
                _uiMessage.tryEmit("Pick an offline station from the list first.")
                return@launch
            }

            val newFav = !playingStation.isFavorite
            stationDao.updateStation(playingStation.copy(isFavorite = newFav))
            if (newFav) {
                _uiMessage.tryEmit("Added ${playingStation.name} to Favorites.")
            } else {
                _uiMessage.tryEmit("Removed ${playingStation.name} to Favorites.")
            }
        }
    }

    fun toggleStationFavorite(station: RadioStation) {
        viewModelScope.launch {
            val newFav = !station.isFavorite
            stationDao.updateStation(station.copy(isFavorite = newFav))
            if (newFav) {
                _uiMessage.tryEmit("Added '${station.name}' to Favorites.")
            } else {
                _uiMessage.tryEmit("Removed '${station.name}' from Favorites.")
            }
        }
    }

    fun setStationFavorite(station: RadioStation, isFavorite: Boolean) {
        if (station.isFavorite == isFavorite) return
        viewModelScope.launch {
            stationDao.updateStation(station.copy(isFavorite = isFavorite))
            if (isFavorite) {
                _uiMessage.tryEmit("Added '${station.name}' to Favorites.")
            } else {
                _uiMessage.tryEmit("Removed '${station.name}' from Favorites.")
            }
        }
    }

    fun toggleCurrentOnlineStationFavorite() {
        val currentId = _currentPlayingOnlineStationId.value
        if (currentId == -1L) {
            _uiMessage.tryEmit("Pick an online station from the list first.")
            return
        }
        viewModelScope.launch {
            val currentStations = allOnlineStationsOnce.value
            val playingStation = currentStations.find { it.id == currentId }

            if (playingStation == null) {
                _uiMessage.tryEmit("Pick an online station from the list first.")
                return@launch
            }

            val newFav = !playingStation.isFavorite
            stationDao.updateStation(playingStation.copy(isFavorite = newFav))
            if (newFav) {
                _uiMessage.tryEmit("Added '${playingStation.name}' to Favorites.")
            } else {
                _uiMessage.tryEmit("Removed '${playingStation.name}' from Favorites.")
            }
        }
    }

    fun addOnlineStation(name: String, streamUrl: String, iconName: String = "Podcasts") {
        val trimmedUrl = streamUrl.trim()
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            _uiMessage.tryEmit("Invalid stream URL. Must start with http:// or https://")
            return
        }

        viewModelScope.launch {
            val existing = stationDao.getOnlineOnce()
            if (existing.any { it.streamUrl.equals(trimmedUrl, ignoreCase = true) }) {
                _uiMessage.tryEmit("This station URL is already in your online list.")
                return@launch
            }

            val maxOrder = existing.maxOfOrNull { it.displayOrder } ?: 0
            val newStation = RadioStation(
                name = name.ifBlank { "Online Station" },
                frequency = "Online",
                isFavorite = false,
                iconName = iconName,
                displayOrder = maxOrder + 1,
                isOnline = true,
                streamUrl = trimmedUrl
            )

            stationDao.insertStation(newStation)
            _uiMessage.tryEmit("Added online station '${newStation.name}'.")
        }
    }

    fun updateStationDetails(
        station: RadioStation,
        newName: String,
        newFrequencyOrUrl: String,
        newIconName: String
    ) {
        if (station.isOnline) {
            val trimmedUrl = newFrequencyOrUrl.trim()
            if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
                _uiMessage.tryEmit("Invalid stream URL. Must start with http:// or https://")
                return
            }

            viewModelScope.launch {
                val existing = stationDao.getOnlineOnce()
                val duplicate = existing.find { it.id != station.id && it.streamUrl.equals(trimmedUrl, ignoreCase = true) }
                if (duplicate != null) {
                    _uiMessage.tryEmit("This URL is already used by ${duplicate.name}.")
                    return@launch
                }

                val updated = station.copy(
                    name = newName.ifBlank { "Online Station" },
                    streamUrl = trimmedUrl,
                    iconName = newIconName
                )
                stationDao.updateStation(updated)

                if (station.id == _currentPlayingOnlineStationId.value) {
                    playOnlineStation(updated)
                }
            }
        } else {
            val newKhz = Freq.parseKhz(newFrequencyOrUrl)
            if (newKhz == null) {
                _uiMessage.tryEmit("Invalid frequency: '$newFrequencyOrUrl'. Range is 76.0 to 108.0 MHz.")
                return
            }

            viewModelScope.launch {
                val all = stationDao.getOfflineOnce()
                val duplicate = all.find { it.id != station.id && Freq.parseKhz(it.frequency) == newKhz }
                if (duplicate != null) {
                    _uiMessage.tryEmit("Frequency ${Freq.formatMhz(newKhz)} is already used by ${duplicate.name}.")
                    return@launch
                }

                val formattedFreq = Freq.formatMhz(newKhz)
                val updated = station.copy(
                    name = newName.ifBlank { "Station" },
                    frequency = formattedFreq,
                    iconName = newIconName
                )

                stationDao.updateStation(updated)

                if (station.id == _currentPlayingStationId.value && _isRadioOn.value) {
                    withContext(Dispatchers.IO) {
                        try {
                            FmEngine.tune(newKhz)
                            applyAudioRoute(_audioRouteState.value)
                            withContext(Dispatchers.Main) {
                                RadioPlaybackService.startService(getApplication(), updated.name, updated.frequency, isOnline = false)
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Throwable) {
                            handleFmError("Lost the FM connection... Make sure Shizuku is still running.")
                        }
                    }
                }
            }
        }
    }

    fun deleteStation(station: RadioStation) {
        viewModelScope.launch {
            if (station.isOnline) {
                if (station.id == _currentPlayingOnlineStationId.value) {
                    stopOnlinePlayback()
                    _uiMessage.tryEmit("Playing online station deleted.")
                }
            } else {
                if (station.id == _currentPlayingStationId.value) {
                    _currentPlayingStationId.value = -1L
                    prefs.currentPlayingId = -1L
                    if (_isRadioOn.value) {
                        executePowerOff()
                        _uiMessage.tryEmit("Playing offline station deleted. Radio turned off.")
                    }
                }
            }
            stationDao.deleteStation(station)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO) {
            executePowerOff()
        }
        exoPlayer?.release()
        exoPlayer = null
        ShizukuHelper.cleanup()
    }
}
