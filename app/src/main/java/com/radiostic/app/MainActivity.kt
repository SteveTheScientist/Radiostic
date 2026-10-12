package com.radiostic.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.radiostic.app.ui.HomeScreen
import com.radiostic.app.ui.LookAndFeelScreen
import com.radiostic.app.ui.MainViewModel
import com.radiostic.app.ui.SettingsScreen
import com.radiostic.app.ui.SplashScreen
import com.radiostic.app.ui.VisualizerScreen
import com.radiostic.app.ui.theme.RadiosticTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        setContent {
            val selectedFont by viewModel.selectedFont.collectAsState()
            val customFontFilePath by viewModel.customFontFilePath.collectAsState()
            val useDynamicColor by viewModel.useDynamicColor.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val textSize by viewModel.textSize.collectAsState()
            val boldText by viewModel.boldText.collectAsState()
            val cornerRoundness by viewModel.cornerRoundness.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val showVisualizer by viewModel.showVisualizer.collectAsState()

            var isSplashFinished by remember { mutableStateOf(false) }

            BackHandler(enabled = (showVisualizer || currentScreen != "Home") && isSplashFinished) {
                if (showVisualizer) {
                    viewModel.closeVisualizer()
                } else {
                    when (currentScreen) {
                        "LookAndFeel" -> viewModel.navigateTo("Settings")
                        "Settings" -> viewModel.navigateTo("Home")
                        else -> viewModel.navigateTo("Home")
                    }
                }
            }

            RadiosticTheme(
                selectedFont = selectedFont,
                customFontFilePath = customFontFilePath,
                useDynamicColor = useDynamicColor,
                themeMode = themeMode,
                textSize = textSize,
                boldText = boldText,
                cornerRoundness = cornerRoundness
            ) {
                if (!isSplashFinished) {
                    SplashScreen(
                        onSplashFinished = {
                            isSplashFinished = true
                        }
                    )
                } else {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            (fadeIn(animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f)))
                                .togetherWith(fadeOut(animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f)))
                        },
                        label = "screenTransition"
                    ) { screen ->
                        when (screen) {
                            "LookAndFeel" -> LookAndFeelScreen(viewModel = viewModel)
                            "Settings" -> SettingsScreen(viewModel = viewModel)
                            else -> HomeScreen(viewModel = viewModel)
                        }
                    }

                    if (showVisualizer) {
                        VisualizerScreen(
                            viewModel = viewModel,
                            onDismiss = { viewModel.closeVisualizer() }
                        )
                    }
                }
            }
        }
    }
}
