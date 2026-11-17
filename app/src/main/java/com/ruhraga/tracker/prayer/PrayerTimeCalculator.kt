package com.ruhraga.tracker.prayer

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class DailyPrayerTimes(
    val subuh: LocalTime,
    val dzuhur: LocalTime,
    val ashar: LocalTime,
    val maghrib: LocalTime,
    val isya: LocalTime
)

data class NextPrayerInfo(
    val id: String,
    val name: String,
    val timeFormatted: String,
    val countdownLabel: String,
    val dateTime: LocalDateTime
)

/**
 * Astronomical prayer time calculator calibrated for Kemenag RI (Fajr 20°, Isha 18°, Shafi'i Asr).
 * Defaults to Jakarta (-6.2088, 106.8456, UTC+7) when GPS location is unavailable.
 */
object PrayerTimeCalculator {

    private const val JAKARTA_LAT = -6.2088
    private const val JAKARTA_LNG = 106.8456
    private const val FAJR_ANGLE = 20.0
    private const val ISHA_ANGLE = 18.0
    private const val IHTIYAT_MINUTES = 2

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    fun calculateForDate(
        date: LocalDate = LocalDate.now(),
        latitude: Double = JAKARTA_LAT,
        longitude: Double = JAKARTA_LNG,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): DailyPrayerTimes {
        val offsetHours = zoneId.rules.getOffset(date.atTime(12, 0)).totalSeconds / 3600.0
        val jd = julianDate(date.year, date.monthValue, date.dayOfMonth) - longitude / (15.0 * 24.0)

        val (declination, eqTime) = sunPosition(jd)
        val transit = 12.0 + offsetHours - longitude / 15.0 - eqTime

        val fajrHour = transit - hourAngle(latitude, declination, -FAJR_ANGLE) / 15.0
        val dhuhrHour = transit + (IHTIYAT_MINUTES / 60.0)
        val asrShadowAngle = Math.toDegrees(atan2(1.0, 1.0 + tan(Math.toRadians(kotlin.math.abs(latitude - declination)))))
        val asrHour = transit + hourAngle(latitude, declination, asrShadowAngle) / 15.0
        val maghribHour = transit + hourAngle(latitude, declination, -0.833) / 15.0 + (IHTIYAT_MINUTES / 60.0)
        val ishaHour = transit + hourAngle(latitude, declination, -ISHA_ANGLE) / 15.0

        return DailyPrayerTimes(
            subuh = hoursToLocalTime(fajrHour),
            dzuhur = hoursToLocalTime(dhuhrHour),
            ashar = hoursToLocalTime(asrHour),
            maghrib = hoursToLocalTime(maghribHour),
            isya = hoursToLocalTime(ishaHour)
        )
    }

    fun findNextPrayer(
        now: LocalDateTime = LocalDateTime.now(),
        todayTimes: DailyPrayerTimes = calculateForDate(now.toLocalDate()),
        tomorrowTimes: DailyPrayerTimes = calculateForDate(now.toLocalDate().plusDays(1))
    ): NextPrayerInfo {
        val today = now.toLocalDate()
        val candidates = listOf(
            Triple("subuh", "Subuh", today.atTime(todayTimes.subuh)),
            Triple("dzuhur", "Dzuhur", today.atTime(todayTimes.dzuhur)),
            Triple("ashar", "Ashar", today.atTime(todayTimes.ashar)),
            Triple("maghrib", "Maghrib", today.atTime(todayTimes.maghrib)),
            Triple("isya", "Isya", today.atTime(todayTimes.isya)),
            Triple("subuh", "Subuh", today.plusDays(1).atTime(tomorrowTimes.subuh))
        )

        val upcoming = candidates.first { it.third.isAfter(now) }
        val duration = Duration.between(now, upcoming.third)
        val totalMinutes = duration.toMinutes().coerceAtLeast(1)
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        val countdown = if (hours > 0) "in ${hours}h ${mins}m" else "in ${mins}m"

        return NextPrayerInfo(
            id = upcoming.first,
            name = upcoming.second,
            timeFormatted = upcoming.third.toLocalTime().format(timeFormatter),
            countdownLabel = countdown,
            dateTime = upcoming.third
        )
    }

    fun formatDateLabel(date: LocalDate = LocalDate.now()): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
        return date.format(formatter).uppercase(Locale.ENGLISH)
    }

    fun formatHijriLabel(date: LocalDate = LocalDate.now()): String {
        val hijrah = HijrahDate.from(date)
        val day = hijrah.get(ChronoField.DAY_OF_MONTH)
        val month = hijrah.get(ChronoField.MONTH_OF_YEAR)
        val monthNames = listOf(
            "Muharram", "Safar", "Rabi'ul Awal", "Rabi'ul Akhir",
            "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban",
            "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
        )
        val monthName = monthNames.getOrElse(month - 1) { "Hijri" }
        return "Hijri $day $monthName"
    }

    fun formatTime(time: LocalTime): String = time.format(timeFormatter)

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(
            atan2(
                cos(Math.toRadians(e)) * sin(Math.toRadians(l)),
                cos(Math.toRadians(l))
            )
        ) / 15.0
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqTime = q / 15.0 - fixHour(ra)
        return decl to eqTime
    }

    private fun hourAngle(lat: Double, decl: Double, angle: Double): Double {
        val numerator = sin(Math.toRadians(angle)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(decl))
        val denominator = cos(Math.toRadians(lat)) * cos(Math.toRadians(decl))
        val ratio = (numerator / denominator).coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(ratio))
    }

    private fun hoursToLocalTime(hours: Double): LocalTime {
        val normalized = fixHour(hours + 0.5 / 60.0)
        val h = floor(normalized).toInt().coerceIn(0, 23)
        val m = floor((normalized - h) * 60.0).toInt().coerceIn(0, 59)
        return LocalTime.of(h, m)
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour % 24.0
        if (h < 0) h += 24.0
        return h
    }
}
