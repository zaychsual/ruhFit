package com.ruhraga.tracker.prayer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ruhraga.tracker.MainActivity
import com.ruhraga.tracker.R

object PrayerNotificationHelper {

    const val CHANNEL_REMINDER = "prayer_reminder_channel"
    const val CHANNEL_ADHAN = "prayer_adhan_azan_v2"

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER,
            "Pengingat Waktu Shalat",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi saat masuk waktu shalat fardhu"
            enableVibration(true)
        }

        val adhanSoundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.azan}")
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val adhanChannel = NotificationChannel(
            CHANNEL_ADHAN,
            "Adzan & Alarm Shalat",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi dengan suara adzan saat waktu shalat tiba"
            setSound(adhanSoundUri, audioAttributes)
            enableVibration(true)
        }

        manager.createNotificationChannel(reminderChannel)
        manager.createNotificationChannel(adhanChannel)
    }

    fun showPrayerNotification(
        context: Context,
        prayerId: String,
        prayerName: String,
        prayerTime: String,
        useAdhanSound: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            prayerId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (useAdhanSound) CHANNEL_ADHAN else CHANNEL_REMINDER
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Waktu Shalat $prayerName ($prayerTime)")
            .setContentText("Mari tunaikan shalat $prayerName tepat waktu. Ruh & Raga terjaga.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        if (useAdhanSound) {
            builder.setSound(Uri.parse("android.resource://${context.packageName}/${R.raw.azan}"))
        }

        NotificationManagerCompat.from(context).notify(prayerId.hashCode(), builder.build())
    }
}
