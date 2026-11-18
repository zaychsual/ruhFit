package com.ruhraga.tracker.ui.state

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruhraga.tracker.data.RuhRagaStorage
import com.ruhraga.tracker.data.WorkoutDatabaseHelper
import com.ruhraga.tracker.prayer.PrayerAlarmScheduler
import com.ruhraga.tracker.prayer.PrayerNotificationHelper
import com.ruhraga.tracker.prayer.PrayerTimeCalculator
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RuhRagaViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ruh_raga_prefs", Context.MODE_PRIVATE)
    private val storage = RuhRagaStorage(application)
    private val workoutDb = WorkoutDatabaseHelper(application)

    private val _uiState = MutableStateFlow(
        storage.restoreInto(
            RuhRagaUiState(
                isDarkTheme = prefs.getBoolean("is_dark_theme", true),
                userProfile = loadSavedProfile(),
                reminderEnabled = prefs.getBoolean("reminder_enabled", true),
                adhanSoundEnabled = prefs.getBoolean("adhan_sound_enabled", false)
            )
        )
    )
    val uiState: StateFlow<RuhRagaUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        refreshPrayerSchedule()
        syncAlarmsWithPreferences()
        startPrayerCountdownTicker()
        refreshWorkoutFromDb()
    }

    private fun refreshWorkoutFromDb() {
        val today = LocalDate.now()
        _uiState.update { state ->
            val exReps = workoutDb.getExerciseRepsForDate(today, state.selectedExercise)
            val totalToday = workoutDb.getTotalRepsForDate(today)
            val sets = workoutDb.getSetsForDateAndExercise(today, state.selectedExercise)
            val history = workoutDb.getRecentWorkoutHistory()
            val allTime = workoutDb.getAllTimeTotalReps()
            state.copy(
                currentExerciseReps = maxOf(exReps, state.currentExerciseReps),
                totalSessionReps = maxOf(totalToday, state.totalSessionReps),
                completedSets = if (sets.isNotEmpty()) sets else state.completedSets,
                currentSetNumber = if (sets.isNotEmpty()) (sets.size + 1).coerceAtMost(state.totalSetsTarget) else state.currentSetNumber,
                workoutHistory = history,
                allTimeWorkoutReps = allTime
            )
        }
    }

    private fun startPrayerCountdownTicker() {
        viewModelScope.launch {
            while (true) {
                delay(30_000L)
                refreshPrayerSchedule()
            }
        }
    }

    private fun refreshPrayerSchedule() {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val nowTime = now.toLocalTime()
        val times = PrayerTimeCalculator.calculateForDate(today)
        val tomorrowTimes = PrayerTimeCalculator.calculateForDate(today.plusDays(1))
        val next = PrayerTimeCalculator.findNextPrayer(now, times, tomorrowTimes)

        val prayerTimesMap = mapOf(
            "subuh" to times.subuh,
            "dzuhur" to times.dzuhur,
            "ashar" to times.ashar,
            "maghrib" to times.maghrib,
            "isya" to times.isya
        )

        val currentWindowId = listOf("isya", "maghrib", "ashar", "dzuhur", "subuh")
            .firstOrNull { id ->
                val t = prayerTimesMap[id] ?: LocalTime.MIDNIGHT
                !nowTime.isBefore(t)
            }

        _uiState.update { state ->
            val updatedPrayers = state.prayers.map { prayer ->
                val targetTime = prayerTimesMap[prayer.id] ?: LocalTime.MIDNIGHT
                val available = !nowTime.isBefore(targetTime)
                val countdownText = if (!available) {
                    val totalMins = Duration.between(now, today.atTime(targetTime))
                        .toMinutes()
                        .coerceAtLeast(1)
                    val hrs = totalMins / 60
                    val mins = totalMins % 60
                    if (hrs > 0) "${hrs}j ${mins}m lagi" else "$mins menit lagi"
                } else {
                    null
                }
                prayer.copy(
                    time = "${PrayerTimeCalculator.formatTime(targetTime)} WIB",
                    isActive = prayer.id == currentWindowId,
                    isAvailable = available,
                    countdownUntilStart = countdownText
                )
            }
            state.copy(
                dateLabel = PrayerTimeCalculator.formatDateLabel(today),
                hijriLabel = PrayerTimeCalculator.formatHijriLabel(today),
                nextPrayerName = next.name,
                nextPrayerTime = next.timeFormatted,
                nextPrayerCountdown = next.countdownLabel,
                prayers = updatedPrayers
            )
        }
    }

    private fun syncAlarmsWithPreferences() {
        val app = getApplication<Application>()
        val state = _uiState.value
        if (state.reminderEnabled) {
            PrayerAlarmScheduler.scheduleDailyPrayers(app, state.adhanSoundEnabled)
        } else {
            PrayerAlarmScheduler.cancelAll(app)
        }
    }

    fun sendTestPrayerNotification() {
        val state = _uiState.value
        PrayerNotificationHelper.showPrayerNotification(
            context = getApplication(),
            prayerId = "test_${state.nextPrayerName}",
            prayerName = state.nextPrayerName,
            prayerTime = state.nextPrayerTime,
            useAdhanSound = state.adhanSoundEnabled
        )
    }

    private fun loadSavedProfile(): UserProfile {
        val completed = prefs.getBoolean("onboarding_completed", false)
        return UserProfile(
            name = prefs.getString("user_name", "") ?: "",
            age = prefs.getInt("user_age", 25),
            weightKg = prefs.getFloat("user_weight_kg", 68f),
            heightCm = prefs.getInt("user_height_cm", 173),
            isOnboardingCompleted = completed
        )
    }

    fun saveUserProfile(
        name: String,
        age: Int,
        weightKg: Float,
        heightCm: Int
    ) {
        val cleanName = name.trim()
        prefs.edit()
            .putString("user_name", cleanName)
            .putInt("user_age", age)
            .putFloat("user_weight_kg", weightKg)
            .putInt("user_height_cm", heightCm)
            .putBoolean("onboarding_completed", true)
            .apply()

        _uiState.update {
            it.copy(
                userProfile = UserProfile(
                    name = cleanName,
                    age = age,
                    weightKg = weightKg,
                    heightCm = heightCm,
                    isOnboardingCompleted = true
                )
            )
        }
    }

    private fun updateAndSyncTodayMatrix(transform: (RuhRagaUiState) -> RuhRagaUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            val todayDay = LocalDate.now().dayOfMonth
            val ruhPct = updated.ruhCompletionPercent
            val ragaPct = updated.ragaCompletionPercent
            val hasRuh = updated.hasRuhActivityToday
            val hasRaga = updated.hasRagaActivityToday

            val status = when {
                hasRuh && hasRaga -> DayMatrixStatus.PERFECT_DUAL
                hasRuh -> DayMatrixStatus.RUH_ONLY
                hasRaga -> DayMatrixStatus.RAGA_ONLY
                else -> DayMatrixStatus.REST
            }

            val summaryParts = buildList {
                if (updated.completedPrayersCount > 0) add("${updated.completedPrayersCount}/5 Shalat")
                if (updated.tilawah.isCompletedToday) add("Tilawah")
                if (updated.totalSessionReps > 0) add("${updated.totalSessionReps} Reps")
                if (updated.cardioDurationMin > 0) add("${updated.cardioDurationMin}m Cardio")
            }
            val summaryText = if (summaryParts.isEmpty()) "Belum ada aktivitas tercatat" else summaryParts.joinToString(", ")

            val updatedMatrix = updated.matrixDays.map { cell ->
                if (cell.day == todayDay) {
                    cell.copy(
                        status = status,
                        ruhPercent = ruhPct,
                        ragaPercent = ragaPct,
                        completionPercent = updated.harmonyPercent,
                        summary = summaryText
                    )
                } else {
                    cell
                }
            }
            val finalState = updated.copy(matrixDays = updatedMatrix)
            storage.saveState(finalState)
            finalState
        }
    }

    fun toggleTheme() {
        val nextDark = !_uiState.value.isDarkTheme
        prefs.edit().putBoolean("is_dark_theme", nextDark).apply()
        _uiState.update { it.copy(isDarkTheme = nextDark) }
    }

    fun toggleReminder() {
        val nextValue = !_uiState.value.reminderEnabled
        prefs.edit().putBoolean("reminder_enabled", nextValue).apply()
        _uiState.update { it.copy(reminderEnabled = nextValue) }
        syncAlarmsWithPreferences()
    }

    fun toggleAdhanSound() {
        val nextValue = !_uiState.value.adhanSoundEnabled
        prefs.edit().putBoolean("adhan_sound_enabled", nextValue).apply()
        _uiState.update { it.copy(adhanSoundEnabled = nextValue) }
        syncAlarmsWithPreferences()
    }

    fun selectIbadahTab(tab: IbadahTab) {
        _uiState.update { it.copy(selectedIbadahTab = tab) }
    }

    fun togglePrayerCompleted(prayerId: String) {
        updateAndSyncTodayMatrix { state ->
            val target = state.prayers.firstOrNull { it.id == prayerId } ?: return@updateAndSyncTodayMatrix state
            if (!target.isAvailable || target.isCompleted) return@updateAndSyncTodayMatrix state

            val updated = state.prayers.map { prayer ->
                if (prayer.id == prayerId) {
                    prayer.copy(
                        isCompleted = true,
                        completedAt = "Completed now"
                    )
                } else {
                    prayer
                }
            }
            state.copy(prayers = updated)
        }
    }

    fun togglePrayerJamaah(prayerId: String) {
        _uiState.update { state ->
            val updated = state.prayers.map { prayer ->
                if (prayer.id == prayerId) prayer.copy(isJamaah = !prayer.isJamaah) else prayer
            }
            val nextState = state.copy(prayers = updated)
            storage.saveState(nextState)
            nextState
        }
    }

    fun logNextPendingPrayer() {
        updateAndSyncTodayMatrix { state ->
            val nextPending = state.prayers.firstOrNull { it.isAvailable && !it.isCompleted }
                ?: return@updateAndSyncTodayMatrix state
            val updated = state.prayers.map { prayer ->
                if (prayer.id == nextPending.id) {
                    prayer.copy(isCompleted = true, completedAt = "Completed now")
                } else {
                    prayer
                }
            }
            state.copy(prayers = updated)
        }
    }

    fun toggleSunnahItem(id: String) {
        updateAndSyncTodayMatrix { state ->
            val updated = state.sunnahItems.map { item ->
                if (item.id == id) item.copy(isCompleted = !item.isCompleted) else item
            }
            state.copy(sunnahItems = updated)
        }
    }

    fun toggleTilawahComplete() {
        updateAndSyncTodayMatrix { state ->
            val completed = !state.tilawah.isCompletedToday
            state.copy(
                tilawah = state.tilawah.copy(
                    isCompletedToday = completed,
                    progressPercent = if (completed) 100 else 0,
                    currentPage = if (completed) state.tilawah.currentPage.coerceAtLeast(1) else 0
                )
            )
        }
    }

    fun tapTasbih() {
        _uiState.update { state ->
            val t = state.tasbih
            val next = when (t.activePhaseIndex) {
                0 -> t.copy(subhanallah = (t.subhanallah + 1).coerceAtMost(t.targetPerPhase))
                1 -> t.copy(alhamdulillah = (t.alhamdulillah + 1).coerceAtMost(t.targetPerPhase))
                else -> {
                    if (t.allahuAkbar < t.targetPerPhase) {
                        t.copy(allahuAkbar = t.allahuAkbar + 1)
                    } else {
                        TasbihState(subhanallah = 0, alhamdulillah = 0, allahuAkbar = 0)
                    }
                }
            }
            val nextState = state.copy(tasbih = next)
            storage.saveState(nextState)
            nextState
        }
    }

    fun selectExercise(category: ExerciseCategory) {
        val today = LocalDate.now()
        val exReps = workoutDb.getExerciseRepsForDate(today, category)
        val sets = workoutDb.getSetsForDateAndExercise(today, category)
        _uiState.update {
            it.copy(
                selectedExercise = category,
                totalSessionTargetReps = category.targetReps,
                activeTimerMovement = category.label,
                currentExerciseReps = exReps,
                completedSets = sets,
                currentSetNumber = (sets.size + 1).coerceAtMost(it.totalSetsTarget)
            )
        }
    }

    fun adjustReps(delta: Int) {
        val today = LocalDate.now()
        val currentState = _uiState.value
        val actualDelta = if (currentState.currentExerciseReps + delta < 0) -currentState.currentExerciseReps else delta
        if (actualDelta != 0) {
            workoutDb.insertWorkoutEntry(
                date = today,
                exercise = currentState.selectedExercise,
                setNumber = 0,
                reps = actualDelta,
                restSeconds = 0
            )
        }
        val history = workoutDb.getRecentWorkoutHistory()
        val allTime = workoutDb.getAllTimeTotalReps()
        updateAndSyncTodayMatrix { state ->
            val updatedReps = (state.currentExerciseReps + delta).coerceAtLeast(0)
            val updatedTotal = (state.totalSessionReps + actualDelta).coerceAtLeast(0)
            state.copy(
                currentExerciseReps = updatedReps,
                totalSessionReps = updatedTotal,
                workoutHistory = history,
                allTimeWorkoutReps = allTime
            )
        }
    }

    fun logSetAndStartRest() {
        val today = LocalDate.now()
        val currentState = _uiState.value
        val repsForSet = currentState.activeTimerReps.takeIf { it > 0 } ?: 10
        val nextSetIndex = currentState.completedSets.size + 1

        workoutDb.insertWorkoutEntry(
            date = today,
            exercise = currentState.selectedExercise,
            setNumber = nextSetIndex,
            reps = repsForSet,
            restSeconds = 45
        )
        val history = workoutDb.getRecentWorkoutHistory()
        val allTime = workoutDb.getAllTimeTotalReps()

        updateAndSyncTodayMatrix { state ->
            val newSet = WorkoutSetItem(
                setNumber = nextSetIndex,
                reps = repsForSet,
                restFormatted = "00:45 rest",
                isDone = true
            )
            state.copy(
                completedSets = state.completedSets + newSet,
                currentSetNumber = (nextSetIndex + 1).coerceAtMost(state.totalSetsTarget),
                currentExerciseReps = state.currentExerciseReps + repsForSet,
                totalSessionReps = state.totalSessionReps + repsForSet,
                workoutHistory = history,
                allTimeWorkoutReps = allTime,
                restSecondsRemaining = 45,
                restTotalSeconds = 45,
                isRestTimerRunning = true
            )
        }
        startRestTicker()
    }

    fun toggleRestTimer() {
        val willRun = !_uiState.value.isRestTimerRunning
        _uiState.update { it.copy(isRestTimerRunning = willRun) }
        if (willRun) {
            startRestTicker()
        } else {
            timerJob?.cancel()
        }
    }

    fun adjustRestSeconds(delta: Int) {
        _uiState.update { state ->
            val next = (state.restSecondsRemaining + delta).coerceIn(0, 300)
            val newTotal = maxOf(state.restTotalSeconds, next).coerceAtLeast(1)
            state.copy(restSecondsRemaining = next, restTotalSeconds = newTotal)
        }
    }

    fun skipRestTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                restSecondsRemaining = 0,
                isRestTimerRunning = false
            )
        }
    }

    fun completeRestAndNextSet() {
        timerJob?.cancel()
        _uiState.update { state ->
            state.copy(
                currentSetNumber = (state.currentSetNumber + 1).coerceAtMost(state.totalSetsTarget),
                restSecondsRemaining = 45,
                restTotalSeconds = 45,
                isRestTimerRunning = true
            )
        }
        startRestTicker()
    }

    fun adjustActiveTimerReps(delta: Int) {
        updateAndSyncTodayMatrix { state ->
            state.copy(
                activeTimerReps = (state.activeTimerReps + delta).coerceAtLeast(0),
                currentExerciseReps = (state.currentExerciseReps + delta).coerceAtLeast(0),
                totalSessionReps = (state.totalSessionReps + delta).coerceAtLeast(0)
            )
        }
    }

    fun toggleAudioCue() {
        _uiState.update { it.copy(audioCueEnabled = !it.audioCueEnabled) }
    }

    fun selectCardioMode(mode: CardioMode) {
        _uiState.update { it.copy(cardioMode = mode) }
    }

    fun logMetabolicFinisher() {
        val today = LocalDate.now()
        val currentState = _uiState.value
        val addDist = if (currentState.cardioMode == CardioMode.RUN) 1.0f else 0.5f
        val addMin = 10
        val addKcal = if (currentState.cardioMode == CardioMode.RUN) 90 else 120

        workoutDb.insertCardioEntry(
            date = today,
            mode = currentState.cardioMode,
            distanceKm = addDist,
            durationMin = addMin,
            calories = addKcal
        )
        val history = workoutDb.getRecentWorkoutHistory()

        updateAndSyncTodayMatrix { state ->
            state.copy(
                cardioDistanceKm = state.cardioDistanceKm + addDist,
                cardioDurationMin = state.cardioDurationMin + addMin,
                cardioCalories = state.cardioCalories + addKcal,
                workoutHistory = history
            )
        }
    }

    fun selectAnalyticsPeriod(period: AnalyticsPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }

    fun selectMatrixDay(day: Int) {
        _uiState.update { it.copy(selectedMatrixDay = day) }
    }

    private fun startRestTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRestTimerRunning && _uiState.value.restSecondsRemaining > 0) {
                delay(1000L)
                _uiState.update { state ->
                    val next = (state.restSecondsRemaining - 1).coerceAtLeast(0)
                    state.copy(
                        restSecondsRemaining = next,
                        isRestTimerRunning = next > 0,
                        activeTimerElapsedSeconds = state.activeTimerElapsedSeconds + 1
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
