package com.ruhraga.tracker.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RuhRagaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RuhRagaUiState())
    val uiState: StateFlow<RuhRagaUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun toggleReminder() {
        _uiState.update { it.copy(reminderEnabled = !it.reminderEnabled) }
    }

    fun toggleAdhanSound() {
        _uiState.update { it.copy(adhanSoundEnabled = !it.adhanSoundEnabled) }
    }

    fun selectIbadahTab(tab: IbadahTab) {
        _uiState.update { it.copy(selectedIbadahTab = tab) }
    }

    fun togglePrayerCompleted(prayerId: String) {
        _uiState.update { state ->
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
        _uiState.update { state ->
            val nextPending = state.prayers.firstOrNull { !it.isCompleted } ?: return@update state
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
        _uiState.update { state ->
            val updated = state.sunnahItems.map { item ->
                if (item.id == id) item.copy(isCompleted = !item.isCompleted) else item
            }
            state.copy(sunnahItems = updated)
        }
    }

    fun toggleTilawahComplete() {
        _uiState.update { state ->
            val completed = !state.tilawah.isCompletedToday
            state.copy(
                tilawah = state.tilawah.copy(
                    isCompletedToday = completed,
                    progressPercent = if (completed) 100 else 27
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
        _uiState.update { it.copy(selectedExercise = category) }
    }

    fun adjustReps(delta: Int) {
        _uiState.update { state ->
            val updatedReps = (state.currentExerciseReps + delta).coerceAtLeast(0)
            val updatedTotal = (state.totalSessionReps + delta).coerceAtLeast(0)
            state.copy(
                currentExerciseReps = updatedReps,
                totalSessionReps = updatedTotal
            )
        }
    }

    fun logSetAndStartRest() {
        _uiState.update { state ->
            val nextSetIndex = state.completedSets.size + 1
            val newSet = WorkoutSetItem(
                setNumber = nextSetIndex,
                reps = 25,
                restFormatted = "00:45 rest",
                isDone = true
            )
            state.copy(
                completedSets = state.completedSets + newSet,
                currentSetNumber = (nextSetIndex + 1).coerceAtMost(state.totalSetsTarget),
                currentExerciseReps = state.currentExerciseReps + 25,
                totalSessionReps = state.totalSessionReps + 25,
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
        _uiState.update { state ->
            state.copy(
                activeTimerReps = (state.activeTimerReps + delta).coerceAtLeast(0),
                currentExerciseReps = (state.currentExerciseReps + delta).coerceAtLeast(0)
            )
        }
    }

    fun toggleAudioCue() {
        _uiState.update { it.copy(audioCueEnabled = !it.audioCueEnabled) }
    }

    fun selectCardioMode(mode: CardioMode) {
        _uiState.update { state ->
            when (mode) {
                CardioMode.RUN -> state.copy(cardioMode = mode, cardioDistanceKm = 3.2f, cardioDurationMin = 15, cardioCalories = 210)
                CardioMode.JUMP_ROPE -> state.copy(cardioMode = mode, cardioDistanceKm = 1.5f, cardioDurationMin = 12, cardioCalories = 185)
            }
        }
    }

    fun logMetabolicFinisher() {
        _uiState.update { state ->
            state.copy(
                cardioDurationMin = state.cardioDurationMin + 5,
                cardioCalories = state.cardioCalories + 65
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
