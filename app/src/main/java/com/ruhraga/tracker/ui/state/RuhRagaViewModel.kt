package com.ruhraga.tracker.ui.state

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruhraga.tracker.prayer.PrayerAlarmScheduler
import com.ruhraga.tracker.prayer.PrayerNotificationHelper
import com.ruhraga.tracker.prayer.PrayerTimeCalculator
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RuhRagaViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ruh_raga_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        RuhRagaUiState(
            userProfile = loadSavedProfile(),
            reminderEnabled = prefs.getBoolean("reminder_enabled", true),
            adhanSoundEnabled = prefs.getBoolean("adhan_sound_enabled", false)
        )
    )
    val uiState: StateFlow<RuhRagaUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        refreshPrayerSchedule()
        syncAlarmsWithPreferences()
        startPrayerCountdownTicker()
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
        val times = PrayerTimeCalculator.calculateForDate(today)
        val tomorrowTimes = PrayerTimeCalculator.calculateForDate(today.plusDays(1))
        val next = PrayerTimeCalculator.findNextPrayer(now, times, tomorrowTimes)

        val formattedTimes = mapOf(
            "subuh" to "${PrayerTimeCalculator.formatTime(times.subuh)} WIB",
            "dzuhur" to "${PrayerTimeCalculator.formatTime(times.dzuhur)} WIB",
            "ashar" to "${PrayerTimeCalculator.formatTime(times.ashar)} WIB",
            "maghrib" to "${PrayerTimeCalculator.formatTime(times.maghrib)} WIB",
            "isya" to "${PrayerTimeCalculator.formatTime(times.isya)} WIB"
        )

        _uiState.update { state ->
            val updatedPrayers = state.prayers.map { prayer ->
                prayer.copy(
                    time = formattedTimes[prayer.id] ?: prayer.time,
                    isActive = prayer.id == next.id
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
            updated.copy(matrixDays = updatedMatrix)
        }
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
            val updated = state.prayers.map { prayer ->
                if (prayer.id == prayerId) {
                    val nowCompleted = !prayer.isCompleted
                    prayer.copy(
                        isCompleted = nowCompleted,
                        completedAt = if (nowCompleted) "Completed now" else null
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
            state.copy(prayers = updated)
        }
    }

    fun logNextPendingPrayer() {
        updateAndSyncTodayMatrix { state ->
            val nextPending = state.prayers.firstOrNull { !it.isCompleted } ?: return@updateAndSyncTodayMatrix state
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
            state.copy(tasbih = next)
        }
    }

    fun selectExercise(category: ExerciseCategory) {
        _uiState.update {
            it.copy(
                selectedExercise = category,
                totalSessionTargetReps = category.targetReps,
                activeTimerMovement = category.label
            )
        }
    }

    fun adjustReps(delta: Int) {
        updateAndSyncTodayMatrix { state ->
            val updatedReps = (state.currentExerciseReps + delta).coerceAtLeast(0)
            val updatedTotal = (state.totalSessionReps + delta).coerceAtLeast(0)
            state.copy(
                currentExerciseReps = updatedReps,
                totalSessionReps = updatedTotal
            )
        }
    }

    fun logSetAndStartRest() {
        updateAndSyncTodayMatrix { state ->
            val repsForSet = state.activeTimerReps.takeIf { it > 0 } ?: 10
            val nextSetIndex = state.completedSets.size + 1
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
        updateAndSyncTodayMatrix { state ->
            val addDist = if (state.cardioMode == CardioMode.RUN) 1.0f else 0.5f
            val addMin = 10
            val addKcal = if (state.cardioMode == CardioMode.RUN) 90 else 120
            state.copy(
                cardioDistanceKm = state.cardioDistanceKm + addDist,
                cardioDurationMin = state.cardioDurationMin + addMin,
                cardioCalories = state.cardioCalories + addKcal
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
