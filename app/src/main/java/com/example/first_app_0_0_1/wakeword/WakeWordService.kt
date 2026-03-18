package com.example.first_app_0_0_1.wakeword

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.first_app_0_0_1.MainActivity
import com.example.first_app_0_0_1.R
import com.example.first_app_0_0_1.common.AppConstants

class WakeWordService : Service() {

    private lateinit var wakeWordDetector: WakeWordDetector

    override fun onCreate() {
        super.onCreate()
        wakeWordDetector = WakeWordDetector(this) {
            Log.d("WakeWordService", "Wake word detected!")
            // Bring app to foreground and send broadcast
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            try {
                pendingIntent.send()
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }

            sendBroadcast(Intent(AppConstants.ACTION_WAKE_WORD_DETECTED))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(WAKE_WORD_NOTIFICATION_ID, createNotification())
        wakeWordDetector.start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeWordDetector.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WAKE_WORD_CHANNEL_ID,
                "Wake Word Detection",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, WAKE_WORD_CHANNEL_ID)
            .setContentTitle("Personal Assistant")
            .setContentText("Listening for wake word...")
            .setSmallIcon(R.mipmap.ic_launcher) // Replace with your app icon
            .build()
    }

    companion object {
        private const val WAKE_WORD_NOTIFICATION_ID = 1
        private const val WAKE_WORD_CHANNEL_ID = "WakeWordChannel"
    }
}
