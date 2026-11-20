package com.ruhraga.tracker.ui.state

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class IbadahTab(val label: String) {
    FARDHU("Fardhu (5)"),
    SUNNAH("Sunnah (4)"),
    TILAWAH_DZIKIR("Tilawah & Dzikir")
}

enum class ExerciseCategory(val label: String, val targetReps: Int) {
    PUSH_UPS("Push-ups", 150),
    PULL_UPS("Pull-ups", 50),
    DIPS("Dips", 60),
    SQUATS("Squats", 100)
}

enum class CardioMode(val label: String) {
    RUN("Run"),
    JUMP_ROPE("Jump Rope")
}

enum class AnalyticsPeriod(val label: String) {
    THIS_MONTH("Bulan Ini"),
    THIS_WEEK("Minggu Ini"),
    ALL_LOG("Semua")
}

enum class DayMatrixStatus {
    REST,
    RUH_ONLY,
    RAGA_ONLY,
    PERFECT_DUAL
}

data class PrayerItem(
        val id: String,
        val name: String,
        val arabicName: String,
        val time: String,
        val rakaat: Int,
        val isCompleted: Boolean = false,
        val isJamaah: Boolean = false,
        val isActive: Boolean = false,
        val isAvailable: Boolean = false,
        val countdownUntilStart: String? = null,
        val completedAt: String? = null
)

data class SunnahItem(
        val id: String,
        val category: String,
        val title: String,
        val detail: String,
        val isCompleted: Boolean = false,
        val progressText: String? = null
)

data class TilawahState(
        val surahTitle: String = "Surah Al-Kahf [18]",
        val juzLabel: String = "Juz 15",
        val arabicVerse: String = "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ",
        val ayatRange: String = "Ayat 1 - 30 of 110 Assigned Today",
        val progressPercent: Int = 0,
        val currentPage: Int = 0,
        val totalPages: Int = 604,
        val isCompletedToday: Boolean = false
)

data class TasbihState(
        val subhanallah: Int = 0,
        val alhamdulillah: Int = 0,
        val allahuAkbar: Int = 0,
        val targetPerPhase: Int = 33
) {
    val activePhaseIndex: Int
        get() =
                when {
                    subhanallah < targetPerPhase -> 0
                    alhamdulillah < targetPerPhase -> 1
                    else -> 2
                }

    val activeCount: Int
        get() =
                when (activePhaseIndex) {
                    0 -> subhanallah
                    1 -> alhamdulillah
                    else -> allahuAkbar
                }
}

data class WorkoutSetItem(
        val setNumber: Int,
        val reps: Int,
        val restFormatted: String,
        val isDone: Boolean
)

data class WorkoutHistoryEntry(
        val date: String,
        val title: String,
        val subtitle: String,
        val metricLabel: String,
        val isCardio: Boolean
)

data class CustomWorkoutItem(
        val id: String,
        val name: String,
        val sets: Int = 3,
        val repsPerSet: Int = 15,
        val isCompletedToday: Boolean = false,
        val isMaster: Boolean = true
) {
    val totalReps: Int
        get() = sets * repsPerSet
}

data class DailyActivityResume(
        val date: LocalDate,
        val ruhPercent: Int,
        val ragaPercent: Int,
        val harmonyPercent: Int,
        val completedPrayers: List<Pair<String, Boolean>>,
        val completedSunnah: List<String>,
        val tilawahDone: Boolean,
        val workoutItems: List<String>,
        val totalReps: Int,
        val cardioSummary: String?
)

data class DayHarmonyPoint(
        val dayLetter: String,
        val ruhDone: Boolean,
        val ragaDone: Boolean,
        val isToday: Boolean = false
)

data class MatrixDayCell(
        val day: Int,
        val status: DayMatrixStatus,
        val ruhPercent: Int,
        val ragaPercent: Int,
        val completionPercent: Int,
        val summary: String
)

data class WeeklyBalanceBar(
        val weekLabel: String,
        val ruhPercent: Float,
        val ragaPercent: Float,
        val isPeak: Boolean = false
)

data class MilestoneItem(
        val title: String,
        val subtitle: String,
        val badge: String,
        val isUnlocked: Boolean,
        val isEmerald: Boolean,
        val progressFraction: Float? = null,
        val progressLabel: String? = null
)

data class UserProfile(
        val name: String = "",
        val age: Int = 25,
        val weightKg: Float = 68f,
        val heightCm: Int = 173,
        val isOnboardingCompleted: Boolean = false
)

data class RuhRagaUiState(
        val isDarkTheme: Boolean = false,
        val userProfile: UserProfile = UserProfile(),
        val dateLabel: String = "",
        val hijriLabel: String = "",
        val nextPrayerName: String = "Subuh",
        val nextPrayerTime: String = "04:30",
        val nextPrayerCountdown: String = "in 0m",
        val locationLabel: String = "Jakarta (WIB) • Qibla 295° NW",
        val milestoneTargetDays: Int = 21,
        val reminderEnabled: Boolean = true,
        val adhanSoundEnabled: Boolean = false,
        val selectedIbadahTab: IbadahTab = IbadahTab.FARDHU,
        val prayers: List<PrayerItem> = defaultPrayers(),
        val sunnahItems: List<SunnahItem> = defaultSunnahItems(),
        val tilawah: TilawahState = TilawahState(),
        val tasbih: TasbihState = TasbihState(),
        val selectedExercise: ExerciseCategory = ExerciseCategory.PUSH_UPS,
        val currentExerciseReps: Int = 0,
        val totalSessionReps: Int = 0,
        val totalSessionTargetReps: Int = 150,
        val currentSetNumber: Int = 1,
        val totalSetsTarget: Int = 6,
        val completedSets: List<WorkoutSetItem> = emptyList(),
        val workoutPlan: List<CustomWorkoutItem> = emptyList(),
        val isWorkoutPlanConfigured: Boolean = false,
        val workoutHistory: List<WorkoutHistoryEntry> = emptyList(),
        val allTimeWorkoutReps: Int = 0,
        val restSecondsRemaining: Int = 0,
        val restTotalSeconds: Int = 45,
        val isRestTimerRunning: Boolean = false,
        val tahajjudAlarmEnabled: Boolean = false,
        val tahajjudAlarmHour: Int = 3,
        val tahajjudAlarmMinute: Int = 30,
        val tahajjudSnoozeCount: Int = 3,
        val tahajjudAlarmVolume: Float = 0.8f,
        val isAlarmPreviewPlaying: Boolean = false,
        val cardioMode: CardioMode = CardioMode.RUN,
        val cardioDistanceKm: Float = 0f,
        val cardioDurationMin: Int = 0,
        val cardioCalories: Int = 0,
        val activeTimerMovement: String = "Push-ups",
        val activeTimerReps: Int = 0,
        val activeTimerElapsedSeconds: Int = 0,
        val activeTimerPulseBpm: Int = 0,
        val audioCueEnabled: Boolean = true,
        val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.THIS_MONTH,
        val matrixDays: List<MatrixDayCell> = defaultEmptyMatrixDays(),
        val displayedYearMonth: YearMonth = YearMonth.now(),
        val displayedMatrixDays: List<MatrixDayCell> = defaultEmptyMatrixDays(),
        val selectedMatrixDay: Int = LocalDate.now().dayOfMonth,
        val selectedDayResume: DailyActivityResume? = null
) {
    val completedPrayersCount: Int
        get() = prayers.count { it.isCompleted }

    val completedSunnahCount: Int
        get() = sunnahItems.count { it.isCompleted }

    val ruhCompletionPercent: Int
        get() {
            val prayerScore =
                    (completedPrayersCount.toFloat() / prayers.size.coerceAtLeast(1)) * 70f
            val tilawahScore = if (tilawah.isCompletedToday) 20f else 0f
            val sunnahScore = if (completedSunnahCount > 0) 10f else 0f
            return (prayerScore + tilawahScore + sunnahScore).toInt().coerceIn(0, 100)
        }

    val ragaCompletionPercent: Int
        get() {
            val planScore =
                    if (workoutPlan.isNotEmpty()) {
                        val done = workoutPlan.count { it.isCompletedToday }
                        (done.toFloat() / workoutPlan.size) * 100f
                    } else {
                        (currentExerciseReps.toFloat() /
                                selectedExercise.targetReps.coerceAtLeast(1)) * 100f
                    }
            val cardioBonus = if (cardioDurationMin > 0) 15f else 0f
            return (planScore + cardioBonus).toInt().coerceIn(0, 100)
        }

    val harmonyPercent: Int
        get() = (ruhCompletionPercent + ragaCompletionPercent) / 2

    val hasRuhActivityToday: Boolean
        get() = completedPrayersCount > 0 || tilawah.isCompletedToday || completedSunnahCount > 0

    val hasRagaActivityToday: Boolean
        get() =
                workoutPlan.any { it.isCompletedToday } ||
                        currentExerciseReps > 0 ||
                        totalSessionReps > 0 ||
                        cardioDurationMin > 0

    val streakDays: Int
        get() = matrixDays.count { it.status != DayMatrixStatus.REST }

    val weeklyHarmony: List<DayHarmonyPoint>
        get() {
            val today = LocalDate.now()
            return (6 downTo 0).map { offset ->
                val dayDate = today.minusDays(offset.toLong())
                val letter = dayDate.dayOfWeek.name.take(1)
                val isToday = offset == 0
                val cell =
                        if (dayDate.month == today.month && dayDate.year == today.year) {
                            matrixDays.firstOrNull { it.day == dayDate.dayOfMonth }
                        } else null

                DayHarmonyPoint(
                        dayLetter = letter,
                        ruhDone = if (isToday) hasRuhActivityToday else (cell?.ruhPercent ?: 0) > 0,
                        ragaDone =
                                if (isToday) hasRagaActivityToday else (cell?.ragaPercent ?: 0) > 0,
                        isToday = isToday
                )
            }
        }

    val weeklySuccessPercent: Int
        get() {
            val totalSlots = weeklyHarmony.size * 2
            val completedSlots =
                    weeklyHarmony.sumOf { (if (it.ruhDone) 1 else 0) + (if (it.ragaDone) 1 else 0) }
            return ((completedSlots.toFloat() / totalSlots) * 100).toInt()
        }

    val monthlyMonthLabel: String
        get() = displayedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))

    val activeLoggedDaysCount: Int
        get() = matrixDays.count { it.status != DayMatrixStatus.REST }

    val avgRuhPercent: Int
        get() {
            val active = matrixDays.filter { it.status != DayMatrixStatus.REST }
            return if (active.isEmpty()) 0 else active.sumOf { it.ruhPercent } / active.size
        }

    val avgRagaPercent: Int
        get() {
            val active = matrixDays.filter { it.status != DayMatrixStatus.REST }
            return if (active.isEmpty()) 0 else active.sumOf { it.ragaPercent } / active.size
        }

    val totalConsistencyPercent: Int
        get() = if (activeLoggedDaysCount == 0) 0 else (avgRuhPercent + avgRagaPercent) / 2

    val weeklyBars: List<WeeklyBalanceBar>
        get() {
            val chunks =
                    listOf(
                            matrixDays.filter { it.day in 1..7 },
                            matrixDays.filter { it.day in 8..14 },
                            matrixDays.filter { it.day in 15..21 },
                            matrixDays.filter { it.day >= 22 }
                    )
            val rawBars =
                    chunks.mapIndexed { index, days ->
                        val ruhAvg =
                                if (days.isEmpty()) 0f
                                else (days.sumOf { it.ruhPercent }.toFloat() / (days.size * 100f))
                        val ragaAvg =
                                if (days.isEmpty()) 0f
                                else (days.sumOf { it.ragaPercent }.toFloat() / (days.size * 100f))
                        WeeklyBalanceBar(
                                weekLabel = "W${index + 1}",
                                ruhPercent = ruhAvg.coerceIn(0f, 1f),
                                ragaPercent = ragaAvg.coerceIn(0f, 1f),
                                isPeak = false
                        )
                    }
            val maxCombined = rawBars.maxOfOrNull { it.ruhPercent + it.ragaPercent } ?: 0f
            return rawBars.map { bar ->
                bar.copy(
                        isPeak =
                                maxCombined > 0f &&
                                        (bar.ruhPercent + bar.ragaPercent) == maxCombined
                )
            }
        }

    val milestones: List<MilestoneItem>
        get() =
                listOf(
                        MilestoneItem(
                                title = "5/5 Shalat Harian",
                                subtitle =
                                        "$completedPrayersCount dari ${prayers.size} shalat fardhu tercatat",
                                badge =
                                        if (completedPrayersCount == prayers.size) "Unlocked"
                                        else "$completedPrayersCount/${prayers.size}",
                                isUnlocked = completedPrayersCount == prayers.size,
                                isEmerald = true,
                                progressFraction =
                                        completedPrayersCount.toFloat() /
                                                prayers.size.coerceAtLeast(1),
                                progressLabel = "$completedPrayersCount/${prayers.size}"
                        ),
                        MilestoneItem(
                                title = "Tilawah Harian",
                                subtitle =
                                        if (tilawah.isCompletedToday) "Tilawah hari ini selesai"
                                        else "Belum menandai tilawah hari ini",
                                badge = if (tilawah.isCompletedToday) "Unlocked" else "Pending",
                                isUnlocked = tilawah.isCompletedToday,
                                isEmerald = true
                        ),
                        MilestoneItem(
                                title = "Centurion Reps",
                                subtitle = "$totalSessionReps / 100 total reps sesi ini",
                                badge =
                                        if (totalSessionReps >= 100) "Unlocked"
                                        else "${(totalSessionReps.coerceAtMost(100))}%",
                                isUnlocked = totalSessionReps >= 100,
                                isEmerald = false,
                                progressFraction = (totalSessionReps / 100f).coerceIn(0f, 1f),
                                progressLabel = "$totalSessionReps/100"
                        ),
                        MilestoneItem(
                                title = "Dual Discipline Day",
                                subtitle = "Catat Ruh & Raga di hari yang sama",
                                badge =
                                        if (hasRuhActivityToday && hasRagaActivityToday) "Unlocked"
                                        else "Active",
                                isUnlocked = hasRuhActivityToday && hasRagaActivityToday,
                                isEmerald = false
                        )
                )
}

private fun defaultPrayers() =
        listOf(
                PrayerItem("subuh", "Subuh", "الفجر", "04:32 WIB", 2),
                PrayerItem("dzuhur", "Dzuhur", "الظهر", "11:58 WIB", 4),
                PrayerItem("ashar", "Ashar", "العصر", "15:15 WIB", 4),
                PrayerItem("maghrib", "Maghrib", "المغرب", "17:58 WIB", 3),
                PrayerItem("isya", "Isya", "العشاء", "19:09 WIB", 4)
        )

private fun defaultSunnahItems() =
        listOf(
                SunnahItem("tahajjud", "Qiyamul Lail", "Tahajjud", "2+ Rakaat"),
                SunnahItem("dhuha", "Morning Nourish", "Dhuha", "2–4 Rakaat"),
                SunnahItem("rawatib", "Sunnah Mu'akkad", "Rawatib", "10 Rakaat"),
                SunnahItem("witir", "Night Cap", "Witir", "3 Rakaat")
        )

private fun defaultEmptyMatrixDays(): List<MatrixDayCell> {
    val totalDays = YearMonth.now().lengthOfMonth()
    return (1..totalDays).map { day ->
        MatrixDayCell(
                day = day,
                status = DayMatrixStatus.REST,
                ruhPercent = 0,
                ragaPercent = 0,
                completionPercent = 0,
                summary = "Belum ada aktivitas tercatat"
        )
    }
}
