package com.radiostic.app.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ShizukuHelper {

    const val SHIZUKU_REQ_CODE = 1001

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted

    private val _isShizukuAvailable = MutableStateFlow(false)
    val isShizukuAvailable: StateFlow<Boolean> = _isShizukuAvailable

    private val onRequestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_REQ_CODE) {
            _isPermissionGranted.value = (grantResult == PackageManager.PERMISSION_GRANTED)
        }
    }

    private val onBinderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkShizukuStatus()
    }

    private val onBinderDeadListener = Shizuku.OnBinderDeadListener {
        _isShizukuAvailable.value = false
        _isPermissionGranted.value = false
    }

    fun init() {
        try {
            Shizuku.addBinderReceivedListenerSticky(onBinderReceivedListener)
            Shizuku.addBinderDeadListener(onBinderDeadListener)
            Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener)
            checkShizukuStatus()
        } catch (e: Throwable) {
            _isShizukuAvailable.value = false
            _isPermissionGranted.value = false
        }
    }

    fun checkShizukuStatus() {
        try {
            val ping = Shizuku.pingBinder()
            _isShizukuAvailable.value = ping
            if (ping) {
                _isPermissionGranted.value = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } else {
                _isPermissionGranted.value = false
            }
        } catch (e: Throwable) {
            _isShizukuAvailable.value = false
            _isPermissionGranted.value = false
        }
    }

    fun requestPermission() {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    _isPermissionGranted.value = true
                } else {
                    Shizuku.requestPermission(SHIZUKU_REQ_CODE)
                }
            } else {
                _isShizukuAvailable.value = false
            }
        } catch (e: Throwable) {
            _isShizukuAvailable.value = false
        }
    }

    fun cleanup() {
        try {
            Shizuku.removeBinderReceivedListener(onBinderReceivedListener)
            Shizuku.removeBinderDeadListener(onBinderDeadListener)
            Shizuku.removeRequestPermissionResultListener(onRequestPermissionResultListener)
        } catch (e: Throwable) {
            // Ignore
        }
    }
}
