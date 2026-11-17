package com.ruhraga.tracker.ui.state

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
    OCT_2023("October 2023"),
    Q3_REVIEW("Q3 Review"),
    YEAR_2023("Year 2023")
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
    val isCompleted: Boolean,
    val isJamaah: Boolean,
    val isActive: Boolean = false,
    val completedAt: String? = null
)

data class SunnahItem(
    val id: String,
    val category: String,
    val title: String,
    val detail: String,
    val isCompleted: Boolean,
    val progressText: String? = null
)

data class TilawahState(
    val surahTitle: String = "Surah Al-Kahf [18]",
    val juzLabel: String = "Juz 15",
    val arabicVerse: String = "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ",
    val ayatRange: String = "Ayat 1 - 30 of 110 Assigned Today",
    val progressPercent: Int = 27,
    val currentPage: Int = 293,
    val totalPages: Int = 604,
    val isCompletedToday: Boolean = false
)

data class TasbihState(
    val subhanallah: Int = 33,
    val alhamdulillah: Int = 33,
    val allahuAkbar: Int = 21,
    val targetPerPhase: Int = 33
) {
    val activePhaseIndex: Int
        get() = when {
            subhanallah < targetPerPhase -> 0
            alhamdulillah < targetPerPhase -> 1
            else -> 2
        }

    val activeCount: Int
        get() = when (activePhaseIndex) {
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

data class DayHarmonyPoint(
    val dayLetter: String,
    val ruhDone: Boolean,
    val ragaDone: Boolean,
    val isToday: Boolean = false
)

data class MatrixDayCell(
    val day: Int,
    val status: DayMatrixStatus,
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
    val isEmerald: Boolean,
    val progressFraction: Float? = null,
    val progressLabel: String? = null
)

data class RuhRagaUiState(
    val userName: String = "Rayyan",
    val dateLabel: String = "MONDAY, 16 OCTOBER",
    val hijriLabel: String = "Hijri 1 Rabi'ul Akhir",
    val streakDays: Int = 14,
    val milestoneTargetDays: Int = 21,
    val reminderEnabled: Boolean = true,
    val adhanSoundEnabled: Boolean = false,
    val selectedIbadahTab: IbadahTab = IbadahTab.FARDHU,
    val prayers: List<PrayerItem> = defaultPrayers(),
    val sunnahItems: List<SunnahItem> = defaultSunnahItems(),
    val tilawah: TilawahState = TilawahState(),
    val tasbih: TasbihState = TasbihState(),
    val selectedExercise: ExerciseCategory = ExerciseCategory.PUSH_UPS,
    val currentExerciseReps: Int = 75,
    val totalSessionReps: Int = 120,
    val totalSessionTargetReps: Int = 200,
    val currentSetNumber: Int = 4,
    val totalSetsTarget: Int = 6,
    val completedSets: List<WorkoutSetItem> = defaultWorkoutSets(),
    val restSecondsRemaining: Int = 45,
    val restTotalSeconds: Int = 60,
    val isRestTimerRunning: Boolean = false,
    val cardioMode: CardioMode = CardioMode.RUN,
    val cardioDistanceKm: Float = 3.2f,
    val cardioDurationMin: Int = 15,
    val cardioCalories: Int = 210,
    val activeTimerMovement: String = "Strict Diamond Push-ups",
    val activeTimerReps: Int = 25,
    val activeTimerElapsedSeconds: Int = 18 * 60 + 42,
    val activeTimerPulseBpm: Int = 142,
    val audioCueEnabled: Boolean = true,
    val weeklyHarmony: List<DayHarmonyPoint> = defaultWeeklyHarmony(),
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.OCT_2023,
    val matrixDays: List<MatrixDayCell> = defaultMatrixDays(),
    val selectedMatrixDay: Int = 16,
    val weeklyBars: List<WeeklyBalanceBar> = defaultWeeklyBars(),
    val milestones: List<MilestoneItem> = defaultMilestones()
) {
    val completedPrayersCount: Int get() = prayers.count { it.isCompleted }
    val ruhCompletionPercent: Int
        get() {
            val prayerWeight = (completedPrayersCount.toFloat() / prayers.size.coerceAtLeast(1)) * 75f
            val tilawahBonus = if (tilawah.isCompletedToday) 25f else 10f
            return (prayerWeight + tilawahBonus).toInt().coerceIn(0, 100)
        }
    val ragaCompletionPercent: Int
        get() = ((currentExerciseReps.toFloat() / selectedExercise.targetReps) * 100f)
            .toInt()
            .coerceIn(0, 100)
    val harmonyPercent: Int get() = ((ruhCompletionPercent + ragaCompletionPercent) / 2)
}

private fun defaultPrayers() = listOf(
    PrayerItem("subuh", "Subuh", "الفجر", "04:32 WIB", 2, isCompleted = true, isJamaah = true, completedAt = "Completed 04:45"),
    PrayerItem("dzuhur", "Dzuhur", "الظهر", "11:58 WIB", 4, isCompleted = true, isJamaah = false, completedAt = "Completed 12:15"),
    PrayerItem("ashar", "Ashar", "العصر", "15:15 WIB", 4, isCompleted = false, isJamaah = true, isActive = true),
    PrayerItem("maghrib", "Maghrib", "المغرب", "17:58 WIB", 3, isCompleted = false, isJamaah = false),
    PrayerItem("isya", "Isya", "العشاء", "19:09 WIB", 4, isCompleted = false, isJamaah = false)
)

private fun defaultSunnahItems() = listOf(
    SunnahItem("tahajjud", "Qiyamul Lail", "Tahajjud", "2 Rakaat • Logged", isCompleted = true),
    SunnahItem("dhuha", "Morning Nourish", "Dhuha", "4 Rakaat • Logged", isCompleted = true),
    SunnahItem("rawatib", "Sunnah Mu'akkad", "Rawatib", "6 / 10 Rakaat", isCompleted = false, progressText = "60%"),
    SunnahItem("witir", "Night Cap", "Witir", "3 Rakaat • Tonight", isCompleted = false)
)

private fun defaultWorkoutSets() = listOf(
    WorkoutSetItem(1, 25, "00:30 rest", isDone = true),
    WorkoutSetItem(2, 25, "00:45 rest", isDone = true),
    WorkoutSetItem(3, 25, "00:45 rest", isDone = true)
)

private fun defaultWeeklyHarmony() = listOf(
    DayHarmonyPoint("T", ruhDone = true, ragaDone = true),
    DayHarmonyPoint("W", ruhDone = true, ragaDone = true),
    DayHarmonyPoint("T", ruhDone = true, ragaDone = true),
    DayHarmonyPoint("F", ruhDone = true, ragaDone = true),
    DayHarmonyPoint("S", ruhDone = true, ragaDone = false),
    DayHarmonyPoint("S", ruhDone = true, ragaDone = true),
    DayHarmonyPoint("M", ruhDone = true, ragaDone = true, isToday = true)
)

private fun defaultMatrixDays(): List<MatrixDayCell> = (1..31).map { day ->
    val status = when (day) {
        1 -> DayMatrixStatus.REST
        2, 6, 11, 13, 19, 25, 27 -> DayMatrixStatus.RUH_ONLY
        7, 21 -> DayMatrixStatus.RAGA_ONLY
        else -> DayMatrixStatus.PERFECT_DUAL
    }
    val pct = when (status) {
        DayMatrixStatus.REST -> 0
        DayMatrixStatus.RUH_ONLY -> 60
        DayMatrixStatus.RAGA_ONLY -> 55
        DayMatrixStatus.PERFECT_DUAL -> 100
    }
    val summary = when (day) {
        16 -> "5/5 Shalat, 120 Push-ups, 5km Run, 4 pgs Quran"
        else -> when (status) {
            DayMatrixStatus.REST -> "Rest & recovery day"
            DayMatrixStatus.RUH_ONLY -> "5/5 Shalat, Tilawah completed • Rest from workout"
            DayMatrixStatus.RAGA_ONLY -> "Calisthenics session • 3/5 Shalat logged"
            DayMatrixStatus.PERFECT_DUAL -> "5/5 Shalat, Calisthenics target & Tilawah complete"
        }
    }
    MatrixDayCell(day, status, pct, summary)
}

private fun defaultWeeklyBars() = listOf(
    WeeklyBalanceBar("W1", 0.68f, 0.56f),
    WeeklyBalanceBar("W2", 0.78f, 0.66f),
    WeeklyBalanceBar("W3", 0.86f, 0.80f),
    WeeklyBalanceBar("W4", 0.96f, 0.92f, isPeak = true)
)

private fun defaultMilestones() = listOf(
    MilestoneItem(
        title = "14-Day Streak Keeper",
        subtitle = "Unbroken dual completion since Oct 3rd",
        badge = "Active",
        isEmerald = false
    ),
    MilestoneItem(
        title = "Fajr Warrior",
        subtitle = "100% Subuh in Jama'ah this month",
        badge = "Unlocked",
        isEmerald = true
    ),
    MilestoneItem(
        title = "Centurion",
        subtitle = "100+ Pushups for 10 days straight",
        badge = "Unlocked",
        isEmerald = false
    ),
    MilestoneItem(
        title = "Half-Khatam Milestone",
        subtitle = "12 / 15 Juz Read (3 Juz remaining)",
        badge = "80%",
        isEmerald = true,
        progressFraction = 0.80f,
        progressLabel = "12/15"
    )
)
