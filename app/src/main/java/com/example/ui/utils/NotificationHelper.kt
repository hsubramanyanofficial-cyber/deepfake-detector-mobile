package com.example.ui.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "sentinel_alerts_channel"
    private const val CHANNEL_NAME = "Sentinel AI deepfake alerts"
    private const val CHANNEL_DESC = "Real-time deepfake alerts and cyberthreat notifications"
    private var isChannelCreated = false

    private fun createNotificationChannel(context: Context) {
        if (isChannelCreated) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        isChannelCreated = true
    }

    fun showNotification(context: Context, title: String, message: String, threatLevel: String = "HIGH") {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationId = System.currentTimeMillis().toInt()

        // Customize sound or visual intensity depending on Threat Level
        val color = when (threatLevel) {
            "CRITICAL" -> 0xFFEF4444.toInt() // Red
            "HIGH" -> 0xFFF97316.toInt()     // Orange
            "WARNING" -> 0xFFEAB308.toInt()  // Yellow
            else -> 0xFF0D9488.toInt()       // Teal
        }

        // Standard launcher icon is used as the small notification icon
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning) // Always-available standard small icon
            .setContentTitle("[$threatLevel ALERT] $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(color)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted or system restricted
            e.printStackTrace()
        }
    }
}
