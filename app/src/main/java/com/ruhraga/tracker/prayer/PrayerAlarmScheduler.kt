package com.ruhraga.tracker.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ruhraga.tracker.MainActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object PrayerAlarmScheduler {

    private data class PrayerTarget(
        val id: String,
        val name: String,
        val time: LocalTime,
        val requestCode: Int
    )

    fun scheduleDailyPrayers(context: Context, useAdhanSound: Boolean) {
        PrayerNotificationHelper.ensureChannels(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val todayTimes = PrayerTimeCalculator.calculateForDate(today)
        val tomorrowTimes = PrayerTimeCalculator.calculateForDate(today.plusDays(1))

        val targets = listOf(
            PrayerTarget("subuh", "Subuh", todayTimes.subuh, 1001),
            PrayerTarget("dzuhur", "Dzuhur", todayTimes.dzuhur, 1002),
            PrayerTarget("ashar", "Ashar", todayTimes.ashar, 1003),
            PrayerTarget("maghrib", "Maghrib", todayTimes.maghrib, 1004),
            PrayerTarget("isya", "Isya", todayTimes.isya, 1005)
        )

        targets.forEach { target ->
            var triggerDateTime = today.atTime(target.time)
            if (!triggerDateTime.isAfter(now)) {
                val tomorrowTime = when (target.id) {
                    "subuh" -> tomorrowTimes.subuh
                    "dzuhur" -> tomorrowTimes.dzuhur
                    "ashar" -> tomorrowTimes.ashar
                    "maghrib" -> tomorrowTimes.maghrib
                    else -> tomorrowTimes.isya
                }
                triggerDateTime = today.plusDays(1).atTime(tomorrowTime)
            }

            val triggerAtMillis = triggerDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_ID, target.id)
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, target.name)
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, PrayerTimeCalculator.formatTime(triggerDateTime.toLocalTime()))
                putExtra(PrayerAlarmReceiver.EXTRA_USE_ADHAN, useAdhanSound)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                target.requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canExact) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    target.requestCode,
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(1001, 1002, 1003, 1004, 1005).forEach { requestCode ->
            val intent = Intent(context, PrayerAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
