package com.radiostic.app.fm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.radiostic.app.MainActivity
import com.radiostic.app.R

class RadioPlaybackService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            RadioPlaybackService.onStopActionCallback?.invoke()
            stopForegroundService()
            return START_NOT_STICKY
        }

        val stationName = intent?.getStringExtra(EXTRA_STATION_NAME) ?: "Station"
        val stationFreq = intent?.getStringExtra(EXTRA_STATION_FREQ) ?: "88.0 MHz"
        val isOnline = intent?.getBooleanExtra(EXTRA_IS_ONLINE, false) ?: false

        val notification = buildNotification(stationName, stationFreq, isOnline)
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Radiostic Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing notification for Radiostic radio playback"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(stationName: String, stationFreq: String, isOnline: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val togglePowerIntent = Intent(this, RadioPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val togglePowerPendingIntent = PendingIntent.getService(
            this, 1, togglePowerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtitle = if (isOnline) "Online Stream • Live" else "$stationFreq • FM Radio Active"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(stationName)
            .setContentText(subtitle)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .setStyle(
                MediaStyle()
                    .setShowActionsInCompactView(0)
            )
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop Radio",
                togglePowerPendingIntent
            )
            .build()
    }

    private fun stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    companion object {
        const val CHANNEL_ID = "radiostic_playback"
        const val NOTIFICATION_ID = 101

        const val ACTION_START = "com.radiostic.app.ACTION_START"
        const val ACTION_STOP = "com.radiostic.app.ACTION_STOP"

        const val EXTRA_STATION_NAME = "extra_station_name"
        const val EXTRA_STATION_FREQ = "extra_station_freq"
        const val EXTRA_IS_ONLINE = "extra_is_online"

        var onStopActionCallback: (() -> Unit)? = null

        fun startService(context: Context, stationName: String, stationFreq: String, isOnline: Boolean = false) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_STATION_NAME, stationName)
                putExtra(EXTRA_STATION_FREQ, stationFreq)
                putExtra(EXTRA_IS_ONLINE, isOnline)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
