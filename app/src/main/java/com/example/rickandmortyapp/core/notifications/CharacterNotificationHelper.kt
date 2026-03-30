package com.example.rickandmortyapp.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.rickandmortyapp.R

class CharacterNotificationHelper(
    private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "character_load_channel"
        const val CHANNEL_NAME = "Character loading"
        const val CHANNEL_DESCRIPTION = "Notifications for character list loading results"

        private const val SUCCESS_NOTIFICATION_ID = 1001
        private const val ERROR_NOTIFICATION_ID = 1002
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showSuccessNotification() {
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Characters loaded")
                .setContentText("Character list loaded successfully.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(context).notify(SUCCESS_NOTIFICATION_ID, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showErrorNotification() {
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Loading failed")
                .setContentText("Failed to load character list.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(context).notify(ERROR_NOTIFICATION_ID, notification)
    }

    fun shouldRequestNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
    }
}
