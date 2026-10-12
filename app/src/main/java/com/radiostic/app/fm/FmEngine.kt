package com.radiostic.app.fm

import android.os.IBinder
import android.os.Parcel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

object FmEngine {

    private const val DESCRIPTOR = "com.samsung.android.media.fmradio.internal.IFMPlayer"

    private var service: IBinder? = null
    val fmEvents = FmEvents()

    private fun getService(): IBinder {
        val current = service
        if (current != null && current.isBinderAlive) {
            return current
        }
        val rawBinder = SystemServiceHelper.getSystemService("FMPlayer")
            ?: throw IllegalStateException("FMPlayer service unavailable")
        val wrapped = ShizukuBinderWrapper(rawBinder)
        service = wrapped
        return wrapped
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            val binder = service
            if (binder != null && binder.isBinderAlive) {
                removeListener(fmEvents)
            }
        } catch (e: Throwable) {
            // Ignore on cleanup
        } finally {
            service = null
        }
    }

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            getService()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            false
        }
    }

    suspend fun setListener(listener: IBinder): Unit = withContext(Dispatchers.IO) {
        transact(1) { data ->
            data.writeStrongBinder(listener)
        }
    }

    suspend fun removeListener(listener: IBinder): Unit = withContext(Dispatchers.IO) {
        transact(2) { data ->
            data.writeStrongBinder(listener)
        }
    }

    suspend fun tune(kHz: Long): Unit = withContext(Dispatchers.IO) {
        transact(3) { data ->
            data.writeLong(kHz)
        }
    }

    suspend fun on(): Boolean = withContext(Dispatchers.IO) {
        transactReadBoolean(4)
    }

    suspend fun off(): Boolean = withContext(Dispatchers.IO) {
        transactReadBoolean(6)
    }

    suspend fun isOn(): Boolean = withContext(Dispatchers.IO) {
        transactReadBoolean(7)
    }

    suspend fun seekUp(): Long = withContext(Dispatchers.IO) {
        transactReadLong(8)
    }

    suspend fun seekDown(): Long = withContext(Dispatchers.IO) {
        transactReadLong(9)
    }

    suspend fun scanAsync(): Unit = withContext(Dispatchers.IO) {
        transact(12)
    }

    suspend fun isScanning(): Boolean = withContext(Dispatchers.IO) {
        transactReadBoolean(14)
    }

    suspend fun enableRDS(): Unit = withContext(Dispatchers.IO) {
        transact(20)
    }

    suspend fun getLastScanResult(): LongArray = withContext(Dispatchers.IO) {
        var results = fetchFrequencies(33) // code 33: getLastScanResult
        if (results.isEmpty()) {
            results = fetchFrequencies(34) // code 34: getChannelList fallback
        }
        results
    }

    private fun fetchFrequencies(code: Int): LongArray {
        val binder = getService()
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DESCRIPTOR)
            binder.transact(code, data, reply, 0)
            reply.readException()

            val pos = reply.dataPosition()

            // Attempt 1: Standard createLongArray()
            val longs = try { reply.createLongArray() } catch (e: Throwable) { null }
            if (longs != null && longs.isNotEmpty()) {
                val valid = longs.filter { it in 76000..108000 || it in 76..108 }
                    .map { if (it in 76..108) it * 1000 else it }
                    .toLongArray()
                if (valid.isNotEmpty()) return valid
            }

            // Attempt 2: createIntArray()
            reply.setDataPosition(pos)
            val ints = try { reply.createIntArray() } catch (e: Throwable) { null }
            if (ints != null && ints.isNotEmpty()) {
                val valid = ints.map { it.toLong() }
                    .filter { it in 76000..108000 || it in 76..108 }
                    .map { if (it in 76..108) it * 1000 else it }
                    .toLongArray()
                if (valid.isNotEmpty()) return valid
            }

            // Attempt 3: Manual Parcel deserialization
            reply.setDataPosition(pos)
            val length = try { reply.readInt() } catch (e: Throwable) { 0 }
            if (length in 1..200) {
                val list = mutableListOf<Long>()
                for (i in 0 until length) {
                    if (reply.dataAvail() >= 8) {
                        val valLong = reply.readLong()
                        if (valLong in 76000..108000) {
                            list.add(valLong)
                        } else if (valLong in 76..108) {
                            list.add(valLong * 1000)
                        }
                    } else if (reply.dataAvail() >= 4) {
                        val valInt = reply.readInt().toLong()
                        if (valInt in 76000..108000) {
                            list.add(valInt)
                        } else if (valInt in 76..108) {
                            list.add(valInt * 1000)
                        }
                    }
                }
                if (list.isNotEmpty()) return list.toLongArray()
            }

            return longArrayOf()
        } catch (e: Throwable) {
            return longArrayOf()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    suspend fun isHeadsetPlugged(): Boolean = withContext(Dispatchers.IO) {
        transactReadBoolean(38)
    }

    suspend fun setSpeakerOn(on: Boolean): Unit = withContext(Dispatchers.IO) {
        transact(40) { data ->
            data.writeInt(if (on) 1 else 0)
        }
    }

    suspend fun mute(mute: Boolean): Unit = withContext(Dispatchers.IO) {
        transact(44) { data ->
            data.writeInt(if (mute) 1 else 0)
        }
    }

    private fun transact(code: Int, writeArgs: ((Parcel) -> Unit)? = null) {
        val binder = getService()
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DESCRIPTOR)
            writeArgs?.invoke(data)
            binder.transact(code, data, reply, 0)
            reply.readException()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    private fun transactReadBoolean(code: Int, writeArgs: ((Parcel) -> Unit)? = null): Boolean {
        val binder = getService()
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DESCRIPTOR)
            writeArgs?.invoke(data)
            binder.transact(code, data, reply, 0)
            reply.readException()
            return reply.readInt() != 0
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    private fun transactReadLong(code: Int, writeArgs: ((Parcel) -> Unit)? = null): Long {
        val binder = getService()
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DESCRIPTOR)
            writeArgs?.invoke(data)
            binder.transact(code, data, reply, 0)
            reply.readException()
            return reply.readLong()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }
}
