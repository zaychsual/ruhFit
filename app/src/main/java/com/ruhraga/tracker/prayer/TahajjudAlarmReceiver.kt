package com.ruhraga.tracker.prayer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ruhraga.tracker.MainActivity
import com.ruhraga.tracker.audio.WorkoutAlarmPlayer

class TahajjudAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val hour = intent.getIntExtra(EXTRA_HOUR, 3)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 30)
        val snoozeRemaining = intent.getIntExtra(EXTRA_SNOOZE_REMAINING, 0)
        val snoozeTotal = intent.getIntExtra(EXTRA_SNOOZE_TOTAL, snoozeRemaining)
        val volume = intent.getFloatExtra(EXTRA_VOLUME, 0.8f)

        when (intent.action) {
            ACTION_DISMISS -> {
                WorkoutAlarmPlayer.stop()
                NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
                return
            }
            ACTION_SNOOZE -> {
                WorkoutAlarmPlayer.stop()
                NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
                if (snoozeRemaining > 0) {
                    TahajjudAlarmScheduler.scheduleSnooze(
                        context = context,
                        hour = hour,
                        minute = minute,
                        snoozeRemaining = snoozeRemaining - 1,
                        snoozeTotal = snoozeTotal,
                        volume = volume
                    )
                }
                return
            }
        }

        WorkoutAlarmPlayer.play(context, volume)
        showTahajjudNotification(context, hour, minute, snoozeRemaining, snoozeTotal, volume)

        // Schedule next day's alarm at the configured time
        TahajjudAlarmScheduler.scheduleDaily(context, hour, minute, snoozeTotal, volume)
    }

    private fun showTahajjudNotification(
        context: Context,
        hour: Int,
        minute: Int,
        snoozeRemaining: Int,
        snoozeTotal: Int,
        volume: Float
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_TAHAJJUD,
            "Alarm Shalat Malam (Tahajjud)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm pengingat bangun shalat malam dengan dering Electric Eel"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)

        val openIntent = PendingIntent.getActivity(
            context,
            2100,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = PendingIntent.getBroadcast(
            context,
            2101,
            Intent(context, TahajjudAlarmReceiver::class.java).apply { action = ACTION_DISMISS },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeFormatted = "%02d:%02d".format(hour, minute)
        val builder = NotificationCompat.Builder(context, CHANNEL_TAHAJJUD)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Waktunya Shalat Malam ($timeFormatted)")
            .setContentText("Bangun untuk Tahajjud & bermunajat kepada Allah.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(0, "Matikan", dismissIntent)

        if (snoozeRemaining > 0) {
            val snoozeIntent = PendingIntent.getBroadcast(
                context,
                2102,
                Intent(context, TahajjudAlarmReceiver::class.java).apply {
                    action = ACTION_SNOOZE
                    putExtra(EXTRA_HOUR, hour)
                    putExtra(EXTRA_MINUTE, minute)
                    putExtra(EXTRA_SNOOZE_REMAINING, snoozeRemaining)
                    putExtra(EXTRA_SNOOZE_TOTAL, snoozeTotal)
                    putExtra(EXTRA_VOLUME, volume)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Snooze 5 mnt ($snoozeRemaining)", snoozeIntent)
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        const val CHANNEL_TAHAJJUD = "tahajjud_alarm_channel"
        const val NOTIFICATION_ID = 2025
        const val ACTION_TRIGGER = "com.ruhraga.tracker.ACTION_TAHAJJUD_TRIGGER"
        const val ACTION_SNOOZE = "com.ruhraga.tracker.ACTION_TAHAJJUD_SNOOZE"
        const val ACTION_DISMISS = "com.ruhraga.tracker.ACTION_TAHAJJUD_DISMISS"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
        const val EXTRA_SNOOZE_REMAINING = "extra_snooze_remaining"
        const val EXTRA_SNOOZE_TOTAL = "extra_snooze_total"
        const val EXTRA_VOLUME = "extra_volume"
    }
}
