package com.ruhraga.tracker.ui.state

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruhraga.tracker.audio.WorkoutAlarmPlayer
import com.ruhraga.tracker.data.RuhRagaStorage
import com.ruhraga.tracker.data.WorkoutDatabaseHelper
import com.ruhraga.tracker.prayer.PrayerAlarmScheduler
import com.ruhraga.tracker.prayer.PrayerNotificationHelper
import com.ruhraga.tracker.prayer.PrayerTimeCalculator
import com.ruhraga.tracker.prayer.TahajjudAlarmScheduler
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
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

    private val _uiState =
            MutableStateFlow(
                    storage.restoreInto(
                            RuhRagaUiState(
                                    isDarkTheme = prefs.getBoolean("is_dark_theme", false),
                                    userProfile = loadSavedProfile(),
                                    reminderEnabled = prefs.getBoolean("reminder_enabled", true),
                                    adhanSoundEnabled =
                                            prefs.getBoolean("adhan_sound_enabled", false),
                                    tahajjudAlarmEnabled =
                                            prefs.getBoolean("tahajjud_alarm_enabled", false),
                                    tahajjudAlarmHour = prefs.getInt("tahajjud_alarm_hour", 3),
                                    tahajjudAlarmMinute = prefs.getInt("tahajjud_alarm_minute", 30),
                                    tahajjudSnoozeCount = prefs.getInt("tahajjud_snooze_count", 3),
                                    tahajjudAlarmVolume =
                                            prefs.getFloat("tahajjud_alarm_volume", 0.8f)
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
                    currentSetNumber =
                            if (sets.isNotEmpty())
                                    (sets.size + 1).coerceAtMost(state.totalSetsTarget)
                            else state.currentSetNumber,
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

        val prayerTimesMap =
                mapOf(
                        "subuh" to times.subuh,
                        "dzuhur" to times.dzuhur,
                        "ashar" to times.ashar,
                        "maghrib" to times.maghrib,
                        "isya" to times.isya
                )

        val currentWindowId =
                listOf("isya", "maghrib", "ashar", "dzuhur", "subuh").firstOrNull { id ->
                    val t = prayerTimesMap[id] ?: LocalTime.MIDNIGHT
                    !nowTime.isBefore(t)
                }

        _uiState.update { state ->
            val updatedPrayers =
                    state.prayers.map { prayer ->
                        val targetTime = prayerTimesMap[prayer.id] ?: LocalTime.MIDNIGHT
                        val available = !nowTime.isBefore(targetTime)
                        val countdownText =
                                if (!available) {
                                    val totalMins =
                                            Duration.between(now, today.atTime(targetTime))
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

    fun saveUserProfile(name: String, age: Int, weightKg: Float, heightCm: Int) {
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
                    userProfile =
                            UserProfile(
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

            val status =
                    when {
                        hasRuh && hasRaga -> DayMatrixStatus.PERFECT_DUAL
                        hasRuh -> DayMatrixStatus.RUH_ONLY
                        hasRaga -> DayMatrixStatus.RAGA_ONLY
                        else -> DayMatrixStatus.REST
                    }

            val summaryParts = buildList {
                if (updated.completedPrayersCount > 0)
                        add("${updated.completedPrayersCount}/5 Shalat")
                if (updated.tilawah.isCompletedToday) add("Tilawah")
                if (updated.totalSessionReps > 0) add("${updated.totalSessionReps} Reps")
                if (updated.cardioDurationMin > 0) add("${updated.cardioDurationMin}m Cardio")
            }
            val summaryText =
                    if (summaryParts.isEmpty()) "Belum ada aktivitas tercatat"
                    else summaryParts.joinToString(", ")

            val updatedMatrix =
                    updated.matrixDays.map { cell ->
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
            val finalState =
                    updated.copy(
                            matrixDays = updatedMatrix,
                            displayedMatrixDays =
                                    if (updated.displayedYearMonth == YearMonth.now()) {
                                        updatedMatrix
                                    } else {
                                        updated.displayedMatrixDays
                                    }
                    )
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
        confirmPrayerCompleted(prayerId, isJamaah = null)
    }

    fun confirmPrayerCompleted(prayerId: String, isJamaah: Boolean? = null) {
        updateAndSyncTodayMatrix { state ->
            val target =
                    state.prayers.firstOrNull { it.id == prayerId }
                            ?: return@updateAndSyncTodayMatrix state
            if (!target.isAvailable || target.isCompleted) return@updateAndSyncTodayMatrix state

            val updated =
                    state.prayers.map { prayer ->
                        if (prayer.id == prayerId) {
                            prayer.copy(
                                    isCompleted = true,
                                    isJamaah = isJamaah ?: prayer.isJamaah,
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
            val target = state.prayers.firstOrNull { it.id == prayerId } ?: return@update state
            if (target.isCompleted) return@update state

            val updated =
                    state.prayers.map { prayer ->
                        if (prayer.id == prayerId) prayer.copy(isJamaah = !prayer.isJamaah)
                        else prayer
                    }
            val nextState = state.copy(prayers = updated)
            storage.saveState(nextState)
            nextState
        }
    }

    fun logNextPendingPrayer() {
        updateAndSyncTodayMatrix { state ->
            val nextPending =
                    state.prayers.firstOrNull { it.isAvailable && !it.isCompleted }
                            ?: return@updateAndSyncTodayMatrix state
            val updated =
                    state.prayers.map { prayer ->
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
            val updated =
                    state.sunnahItems.map { item ->
                        if (item.id == id) item.copy(isCompleted = !item.isCompleted) else item
                    }
            state.copy(sunnahItems = updated)
        }
    }

    fun toggleTilawahComplete() {
        updateAndSyncTodayMatrix { state ->
            val completed = !state.tilawah.isCompletedToday
            state.copy(
                    tilawah =
                            state.tilawah.copy(
                                    isCompletedToday = completed,
                                    progressPercent = if (completed) 100 else 0,
                                    currentPage =
                                            if (completed)
                                                    state.tilawah.currentPage.coerceAtLeast(1)
                                            else 0
                            )
            )
        }
    }

    fun tapTasbih() {
        _uiState.update { state ->
            val t = state.tasbih
            val next =
                    when (t.activePhaseIndex) {
                        0 ->
                                t.copy(
                                        subhanallah =
                                                (t.subhanallah + 1).coerceAtMost(t.targetPerPhase)
                                )
                        1 ->
                                t.copy(
                                        alhamdulillah =
                                                (t.alhamdulillah + 1).coerceAtMost(t.targetPerPhase)
                                )
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

    fun addWorkoutPlanItem(name: String, sets: Int, repsPerSet: Int) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        val newItem =
                CustomWorkoutItem(
                        id =
                                "${cleanName.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}",
                        name = cleanName,
                        sets = sets.coerceIn(1, 20),
                        repsPerSet = repsPerSet.coerceIn(1, 200)
                )
        updateAndSyncTodayMatrix { state -> state.copy(workoutPlan = state.workoutPlan + newItem) }
    }

    fun removeWorkoutPlanItem(id: String) {
        updateAndSyncTodayMatrix { state ->
            state.copy(workoutPlan = state.workoutPlan.filterNot { it.id == id })
        }
    }

    fun finishWorkoutPlanSetup() {
        updateAndSyncTodayMatrix { state ->
            if (state.workoutPlan.isEmpty()) state else state.copy(isWorkoutPlanConfigured = true)
        }
    }

    fun reopenWorkoutPlanSetup() {
        _uiState.update { it.copy(isWorkoutPlanConfigured = false) }
    }

    fun updatePlanItemSets(id: String, delta: Int) {
        updateAndSyncTodayMatrix { state ->
            val updated =
                    state.workoutPlan.map { item ->
                        if (item.id == id && !item.isCompletedToday) {
                            item.copy(sets = (item.sets + delta).coerceIn(1, 20))
                        } else item
                    }
            state.copy(workoutPlan = updated)
        }
    }

    fun updatePlanItemReps(id: String, delta: Int) {
        updateAndSyncTodayMatrix { state ->
            val updated =
                    state.workoutPlan.map { item ->
                        if (item.id == id && !item.isCompletedToday) {
                            item.copy(repsPerSet = (item.repsPerSet + delta).coerceIn(1, 200))
                        } else item
                    }
            state.copy(workoutPlan = updated)
        }
    }

    fun togglePlanItemCompleted(id: String) {
        val today = LocalDate.now()
        val current = _uiState.value
        val target = current.workoutPlan.firstOrNull { it.id == id } ?: return
        if (target.isCompletedToday) return

        val matchedCategory =
                ExerciseCategory.entries.firstOrNull {
                    target.name.contains(it.label.removeSuffix("s"), ignoreCase = true)
                }
                        ?: ExerciseCategory.PUSH_UPS

        workoutDb.insertWorkoutEntry(
                date = today,
                exercise = matchedCategory,
                setNumber = target.sets,
                reps = target.totalReps,
                restSeconds = 45
        )
        val history = workoutDb.getRecentWorkoutHistory()
        val allTime = workoutDb.getAllTimeTotalReps()

        updateAndSyncTodayMatrix { state ->
            val updatedPlan =
                    state.workoutPlan.map { item ->
                        if (item.id == id) item.copy(isCompletedToday = true) else item
                    }
            state.copy(
                    workoutPlan = updatedPlan,
                    totalSessionReps = state.totalSessionReps + target.totalReps,
                    workoutHistory = history,
                    allTimeWorkoutReps = allTime
            )
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
        val actualDelta =
                if (currentState.currentExerciseReps + delta < 0) -currentState.currentExerciseReps
                else delta
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
            val newSet =
                    WorkoutSetItem(
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

    private fun syncTahajjudAlarm() {
        val app = getApplication<Application>()
        val state = _uiState.value
        if (state.tahajjudAlarmEnabled) {
            TahajjudAlarmScheduler.scheduleDaily(
                    context = app,
                    hour = state.tahajjudAlarmHour,
                    minute = state.tahajjudAlarmMinute,
                    snoozeCount = state.tahajjudSnoozeCount,
                    volume = state.tahajjudAlarmVolume
            )
        } else {
            TahajjudAlarmScheduler.cancelAll(app)
        }
    }

    fun toggleTahajjudAlarm() {
        val next = !_uiState.value.tahajjudAlarmEnabled
        prefs.edit().putBoolean("tahajjud_alarm_enabled", next).apply()
        if (!next) {
            WorkoutAlarmPlayer.stop()
        }
        _uiState.update {
            it.copy(
                    tahajjudAlarmEnabled = next,
                    isAlarmPreviewPlaying = if (next) it.isAlarmPreviewPlaying else false
            )
        }
        syncTahajjudAlarm()
    }

    fun adjustTahajjudTime(deltaHour: Int, deltaMinute: Int) {
        _uiState.update { state ->
            val newHour = (state.tahajjudAlarmHour + deltaHour + 24) % 24
            val newMinute = (state.tahajjudAlarmMinute + deltaMinute + 60) % 60
            prefs.edit()
                    .putInt("tahajjud_alarm_hour", newHour)
                    .putInt("tahajjud_alarm_minute", newMinute)
                    .apply()
            state.copy(tahajjudAlarmHour = newHour, tahajjudAlarmMinute = newMinute)
        }
        syncTahajjudAlarm()
    }

    fun adjustTahajjudSnooze(delta: Int) {
        _uiState.update { state ->
            val next = (state.tahajjudSnoozeCount + delta).coerceIn(0, 10)
            prefs.edit().putInt("tahajjud_snooze_count", next).apply()
            state.copy(tahajjudSnoozeCount = next)
        }
        syncTahajjudAlarm()
    }

    fun setTahajjudAlarmVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat("tahajjud_alarm_volume", clamped).apply()
        WorkoutAlarmPlayer.updateVolume(clamped)
        _uiState.update { it.copy(tahajjudAlarmVolume = clamped) }
        syncTahajjudAlarm()
    }

    fun toggleAlarmPreview() {
        val state = _uiState.value
        if (state.isAlarmPreviewPlaying) {
            WorkoutAlarmPlayer.stop()
            _uiState.update { it.copy(isAlarmPreviewPlaying = false) }
        } else {
            _uiState.update { it.copy(isAlarmPreviewPlaying = true) }
            WorkoutAlarmPlayer.play(
                    context = getApplication(),
                    volume = state.tahajjudAlarmVolume,
                    onCompleted = { _uiState.update { it.copy(isAlarmPreviewPlaying = false) } }
            )
        }
    }

    fun skipRestTimer() {
        timerJob?.cancel()
        WorkoutAlarmPlayer.stop()
        _uiState.update {
            it.copy(
                    restSecondsRemaining = 0,
                    isRestTimerRunning = false,
                    isAlarmPreviewPlaying = false
            )
        }
    }

    fun completeRestAndNextSet() {
        timerJob?.cancel()
        _uiState.update { state ->
            state.copy(
                    currentSetNumber =
                            (state.currentSetNumber + 1).coerceAtMost(state.totalSetsTarget),
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
        // Rumus MET (Compendium of Physical Activities):
        // Kalori = MET * Berat Badan (kg) * Durasi (jam)
        val met = if (currentState.cardioMode == CardioMode.RUN) 9.8f else 11.8f
        val weightKg = currentState.userProfile.weightKg.coerceAtLeast(30f)
        val addKcal = kotlin.math.round(met * weightKg * (addMin / 60f)).toInt()

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

    fun navigateAnalyticsMonth(deltaMonths: Long) {
        _uiState.update { state ->
            val currentMonth = YearMonth.now()
            val targetMonth = state.displayedYearMonth.plusMonths(deltaMonths)
            if (targetMonth.isAfter(currentMonth)) return@update state

            val days =
                    if (targetMonth == currentMonth) {
                        state.matrixDays
                    } else {
                        storage.loadMatrixForMonth(targetMonth)
                    }
            val defaultSelected = if (targetMonth == currentMonth) LocalDate.now().dayOfMonth else 1
            state.copy(
                    displayedYearMonth = targetMonth,
                    displayedMatrixDays = days,
                    selectedMatrixDay = defaultSelected
            )
        }
    }

    fun openDayResume(day: Int) {
        val state = _uiState.value
        val targetDate =
                state.displayedYearMonth.atDay(
                        day.coerceIn(1, state.displayedYearMonth.lengthOfMonth())
                )
        val cell = state.displayedMatrixDays.firstOrNull { it.day == day }
        val resume = storage.loadDailyActivityResume(targetDate, state.workoutPlan, cell)
        _uiState.update { it.copy(selectedMatrixDay = day, selectedDayResume = resume) }
    }

    fun dismissDayResume() {
        _uiState.update { it.copy(selectedDayResume = null) }
    }

    private fun startRestTicker() {
        timerJob?.cancel()
        timerJob =
                viewModelScope.launch {
                    while (_uiState.value.isRestTimerRunning &&
                            _uiState.value.restSecondsRemaining > 0) {
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
        WorkoutAlarmPlayer.stop()
    }
}
