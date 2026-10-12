package com.radiostic.app.fm

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

data class RdsEvent(val kHz: Long, val stationName: String?, val radioText: String?)

class FmEvents : Binder(), IInterface {

    private val DESCRIPTOR = "com.samsung.android.media.fmradio.internal.IFMEventListener"

    private val _tunedKhz = MutableStateFlow<Long?>(null)
    val tunedKhz: StateFlow<Long?> = _tunedKhz.asStateFlow()

    private val _rdsFlow = MutableSharedFlow<RdsEvent>(extraBufferCapacity = 16)
    val rdsFlow: SharedFlow<RdsEvent> = _rdsFlow.asSharedFlow()

    init {
        attachInterface(this, DESCRIPTOR)
    }

    override fun asBinder(): IBinder = this

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (code in 1..18) {
            try {
                data.enforceInterface(DESCRIPTOR)
                when (code) {
                    7 -> { // onTuned(long kHz)
                        val kHz = data.readLong()
                        _tunedKhz.value = kHz
                    }
                    10 -> { // onRadioDataSystemReceived(long kHz, String stationName, String radioText)
                        val kHz = data.readLong()
                        val stationName = data.readString()
                        val radioText = data.readString()
                        _rdsFlow.tryEmit(RdsEvent(kHz, stationName, radioText))
                    }
                }
                reply?.writeNoException()
                return true
            } catch (e: Throwable) {
                reply?.writeNoException()
                return true
            }
        }
        return super.onTransact(code, data, reply, flags)
    }
}
