package com.example.anadolugalericilersit.utils

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.anadolugalericilersit.AnadoluApp
import com.example.anadolugalericilersit.MainActivity
import com.example.anadolugalericilersit.data.model.Dealer

actual object NotificationUtils {
    private const val CHANNEL_ID = "debt_reminders"
    private const val CHANNEL_NAME = "Borç Hatırlatmaları"

    actual fun sendDebtNotification(title: String, message: String) {
        try {
            val ctx = AnadoluApp.instance
            val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }

            val intent = Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            manager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun sendDebtOrPaymentNotification(context: Any?, dealer: Dealer, type: String, amount: Double) {
        val title = if (type == "DEBT") "Borç Eklendi" else "Ödeme Yapıldı"
        val message = "${dealer.galleryName} için $amount TL $title işlemi yapıldı."
        sendDebtNotification(title, message)
    }
}
