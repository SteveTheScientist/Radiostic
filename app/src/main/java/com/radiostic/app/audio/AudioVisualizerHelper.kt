package com.radiostic.app.audio

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class AudioVisualizerHelper {

    private var visualizer: Visualizer? = null

    private val numBars = 32
    private val rawData = FloatArray(numBars) { 0f }
    private val smoothedData = FloatArray(numBars) { 0f }

    private val _fftData = MutableStateFlow(FloatArray(numBars) { 0f })
    val fftData: StateFlow<FloatArray> = _fftData.asStateFlow()

    private var lastUpdateTime = 0L

    fun start(audioSessionId: Int = 0) {
        stop()
        try {
            val vis = Visualizer(audioSessionId)
            val captureRange = Visualizer.getCaptureSizeRange()
            vis.captureSize = captureRange[1]
            vis.setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {}

                    override fun onFftDataCapture(
                        visualizer: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        if (fft != null && fft.isNotEmpty()) {
                            processFftFast(fft)
                        }
                    }
                },
                Visualizer.getMaxCaptureRate(),
                false,
                true
            )
            vis.enabled = true
            visualizer = vis
        } catch (e: Exception) {
            // Audio permission or hardware audio session fallback
        }
    }

    private fun processFftFast(fft: ByteArray) {
        val now = System.currentTimeMillis()
        if (now - lastUpdateTime < 12) return // Throttle to ~60-80 fps to prevent state flow overload
        lastUpdateTime = now

        for (i in 0 until numBars) {
            val r = fft[i * 2].toFloat()
            val im = fft[i * 2 + 1].toFloat()
            val magnitude = sqrt(r * r + im * im)
            val normalized = (magnitude / 128f).coerceIn(0f, 1f)

            // Lightweight low-pass filter
            smoothedData[i] = smoothedData[i] * 0.65f + normalized * 0.35f
        }

        _fftData.value = smoothedData.copyOf()
    }

    fun stop() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            visualizer = null
        }
    }
}
