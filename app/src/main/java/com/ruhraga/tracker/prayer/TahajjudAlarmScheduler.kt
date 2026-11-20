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

object TahajjudAlarmScheduler {

    private const val REQUEST_CODE_MAIN = 2001
    private const val REQUEST_CODE_SNOOZE = 2002

    fun scheduleDaily(
        context: Context,
        hour: Int,
        minute: Int,
        snoozeCount: Int,
        volume: Float
    ) {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        var triggerDateTime = today.atTime(LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59)))
        if (!triggerDateTime.isAfter(now)) {
            triggerDateTime = today.plusDays(1).atTime(triggerDateTime.toLocalTime())
        }

        val triggerAtMillis = triggerDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, TahajjudAlarmReceiver::class.java).apply {
            action = TahajjudAlarmReceiver.ACTION_TRIGGER
            putExtra(TahajjudAlarmReceiver.EXTRA_HOUR, hour)
            putExtra(TahajjudAlarmReceiver.EXTRA_MINUTE, minute)
            putExtra(TahajjudAlarmReceiver.EXTRA_SNOOZE_REMAINING, snoozeCount)
            putExtra(TahajjudAlarmReceiver.EXTRA_SNOOZE_TOTAL, snoozeCount)
            putExtra(TahajjudAlarmReceiver.EXTRA_VOLUME, volume)
        }
        scheduleExactOrAllowIdle(context, REQUEST_CODE_MAIN, triggerAtMillis, intent)
    }

    fun scheduleSnooze(
        context: Context,
        hour: Int,
        minute: Int,
        snoozeRemaining: Int,
        snoozeTotal: Int,
        volume: Float
    ) {
        val triggerAtMillis = System.currentTimeMillis() + 5 * 60 * 1000L
        val intent = Intent(context, TahajjudAlarmReceiver::class.java).apply {
            action = TahajjudAlarmReceiver.ACTION_TRIGGER
            putExtra(TahajjudAlarmReceiver.EXTRA_HOUR, hour)
            putExtra(TahajjudAlarmReceiver.EXTRA_MINUTE, minute)
            putExtra(TahajjudAlarmReceiver.EXTRA_SNOOZE_REMAINING, snoozeRemaining)
            putExtra(TahajjudAlarmReceiver.EXTRA_SNOOZE_TOTAL, snoozeTotal)
            putExtra(TahajjudAlarmReceiver.EXTRA_VOLUME, volume)
        }
        scheduleExactOrAllowIdle(context, REQUEST_CODE_SNOOZE, triggerAtMillis, intent)
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(REQUEST_CODE_MAIN, REQUEST_CODE_SNOOZE).forEach { code ->
            val intent = Intent(context, TahajjudAlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pi != null) {
                alarmManager.cancel(pi)
                pi.cancel()
            }
        }
    }

    private fun scheduleExactOrAllowIdle(
        context: Context,
        requestCode: Int,
        triggerAtMillis: Long,
        intent: Intent
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
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
                requestCode,
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
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }
}
