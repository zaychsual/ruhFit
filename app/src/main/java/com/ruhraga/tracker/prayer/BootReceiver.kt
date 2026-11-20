package com.ruhraga.tracker.prayer

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val validActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        )
        if (action !in validActions) return

        val prefs = context.getSharedPreferences("ruh_raga_prefs", Context.MODE_PRIVATE)
        val reminderEnabled = prefs.getBoolean("reminder_enabled", true)
        val adhanSoundEnabled = prefs.getBoolean("adhan_sound_enabled", false)

        if (reminderEnabled) {
            PrayerAlarmScheduler.scheduleDailyPrayers(context, adhanSoundEnabled)
        }

        val tahajjudEnabled = prefs.getBoolean("tahajjud_alarm_enabled", false)
        if (tahajjudEnabled) {
            val hour = prefs.getInt("tahajjud_alarm_hour", 3)
            val minute = prefs.getInt("tahajjud_alarm_minute", 30)
            val snoozeCount = prefs.getInt("tahajjud_snooze_count", 3)
            val volume = prefs.getFloat("tahajjud_alarm_volume", 0.8f)
            TahajjudAlarmScheduler.scheduleDaily(
                context = context,
                hour = hour,
                minute = minute,
                snoozeCount = snoozeCount,
                volume = volume
            )
        }
    }
}
