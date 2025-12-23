package com.example.akllkampssalkvegvenlikbildirimuygulamas.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.main.MainActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.PermissionUtils

object NotificationHelper {

    const val CHANNEL_ID = "campus_alerts"
    private const val CHANNEL_NAME = "Kampüs Uyarıları"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Durum değişiklikleri ve acil duyurular"
        nm.createNotificationChannel(channel)
    }

    fun showLocalNotification(context: Context, title: String, body: String) {
        // Android 13+ runtime permission
        if (!PermissionUtils.hasPostNotifications(context)) return

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val piFlags = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 100, intent, piFlags)

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, n)
    }
}
