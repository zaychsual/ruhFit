package com.ruhraga.tracker.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerId = intent.getStringExtra(EXTRA_PRAYER_ID) ?: return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Shalat"
        val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
        val useAdhan = intent.getBooleanExtra(EXTRA_USE_ADHAN, false)

        PrayerNotificationHelper.showPrayerNotification(
            context = context,
            prayerId = prayerId,
            prayerName = prayerName,
            prayerTime = prayerTime,
            useAdhanSound = useAdhan
        )

        // Reschedule all daily alarms for the next upcoming occurrences
        PrayerAlarmScheduler.scheduleDailyPrayers(context, useAdhan)
    }

    companion object {
        const val EXTRA_PRAYER_ID = "extra_prayer_id"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_USE_ADHAN = "extra_use_adhan"
    }
}
