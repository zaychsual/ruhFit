package com.ruhraga.tracker.data

import android.content.Context
import com.ruhraga.tracker.ui.state.CardioMode
import com.ruhraga.tracker.ui.state.CustomWorkoutItem
import com.ruhraga.tracker.ui.state.DayMatrixStatus
import com.ruhraga.tracker.ui.state.ExerciseCategory
import com.ruhraga.tracker.ui.state.MatrixDayCell
import com.ruhraga.tracker.ui.state.PrayerItem
import com.ruhraga.tracker.ui.state.RuhRagaUiState
import com.ruhraga.tracker.ui.state.SunnahItem
import com.ruhraga.tracker.ui.state.TasbihState
import com.ruhraga.tracker.ui.state.TilawahState
import com.ruhraga.tracker.ui.state.WorkoutSetItem
import java.time.LocalDate
import java.time.YearMonth
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local persistence for daily activity logs and monthly discipline matrix
 * using SharedPreferences + org.json so history survives app restarts and day rollovers.
 */
class RuhRagaStorage(context: Context) {

    private val prefs = context.getSharedPreferences("ruh_raga_activity_prefs", Context.MODE_PRIVATE)

    fun saveState(state: RuhRagaUiState, date: LocalDate = LocalDate.now()) {
        val monthKey = "matrix_${YearMonth.from(date)}"
        val dayKey = "daily_log_$date"

        val matrixArray = JSONArray()
        state.matrixDays.forEach { cell ->
            matrixArray.put(
                JSONObject().apply {
                    put("day", cell.day)
                    put("status", cell.status.name)
                    put("ruhPercent", cell.ruhPercent)
                    put("ragaPercent", cell.ragaPercent)
                    put("completionPercent", cell.completionPercent)
                    put("summary", cell.summary)
                }
            )
        }

        val dailyJson = JSONObject().apply {
            val prayersArray = JSONArray()
            state.prayers.forEach { p ->
                prayersArray.put(
                    JSONObject().apply {
                        put("id", p.id)
                        put("isCompleted", p.isCompleted)
                        put("isJamaah", p.isJamaah)
                        put("completedAt", p.completedAt ?: "")
                    }
                )
            }
            put("prayers", prayersArray)

            val sunnahArray = JSONArray()
            state.sunnahItems.forEach { s ->
                sunnahArray.put(
                    JSONObject().apply {
                        put("id", s.id)
                        put("isCompleted", s.isCompleted)
                    }
                )
            }
            put("sunnah", sunnahArray)

            put(
                "tilawah",
                JSONObject().apply {
                    put("isCompletedToday", state.tilawah.isCompletedToday)
                    put("progressPercent", state.tilawah.progressPercent)
                    put("currentPage", state.tilawah.currentPage)
                }
            )

            put(
                "tasbih",
                JSONObject().apply {
                    put("subhanallah", state.tasbih.subhanallah)
                    put("alhamdulillah", state.tasbih.alhamdulillah)
                    put("allahuAkbar", state.tasbih.allahuAkbar)
                }
            )

            put("selectedExercise", state.selectedExercise.name)
            put("currentExerciseReps", state.currentExerciseReps)
            put("totalSessionReps", state.totalSessionReps)
            put("currentSetNumber", state.currentSetNumber)
            put("activeTimerReps", state.activeTimerReps)

            val setsArray = JSONArray()
            state.completedSets.forEach { set ->
                setsArray.put(
                    JSONObject().apply {
                        put("setNumber", set.setNumber)
                        put("reps", set.reps)
                        put("restFormatted", set.restFormatted)
                        put("isDone", set.isDone)
                    }
                )
            }
            put("completedSets", setsArray)

            put("cardioMode", state.cardioMode.name)
            put("cardioDistanceKm", state.cardioDistanceKm.toDouble())
            put("cardioDurationMin", state.cardioDurationMin)
            put("cardioCalories", state.cardioCalories)

            val planStatusArray = JSONArray()
            state.workoutPlan.forEach { item ->
                planStatusArray.put(
                    JSONObject().apply {
                        put("id", item.id)
                        put("sets", item.sets)
                        put("repsPerSet", item.repsPerSet)
                        put("isCompletedToday", item.isCompletedToday)
                    }
                )
            }
            put("workoutPlanDaily", planStatusArray)
        }

        val globalPlanArray = JSONArray()
        state.workoutPlan.forEach { item ->
            globalPlanArray.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("sets", item.sets)
                    put("repsPerSet", item.repsPerSet)
                }
            )
        }

        prefs.edit()
            .putString(monthKey, matrixArray.toString())
            .putString(dayKey, dailyJson.toString())
            .putString("workout_plan_list", globalPlanArray.toString())
            .putBoolean("workout_plan_configured", state.isWorkoutPlanConfigured)
            .apply()
    }

    fun restoreInto(baseState: RuhRagaUiState, date: LocalDate = LocalDate.now()): RuhRagaUiState {
        var result = restoreWorkoutPlan(baseState)
        result = restoreMatrix(result, YearMonth.from(date))
        result = restoreDailyLog(result, date)
        return result
    }

    private fun restoreWorkoutPlan(state: RuhRagaUiState): RuhRagaUiState {
        val configured = prefs.getBoolean("workout_plan_configured", false)
        val raw = prefs.getString("workout_plan_list", null) ?: return state.copy(isWorkoutPlanConfigured = configured)
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<CustomWorkoutItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CustomWorkoutItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        sets = obj.optInt("sets", 3),
                        repsPerSet = obj.optInt("repsPerSet", 15),
                        isCompletedToday = false
                    )
                )
            }
            state.copy(workoutPlan = list, isWorkoutPlanConfigured = configured)
        } catch (_: Exception) {
            state
        }
    }

    private fun restoreMatrix(state: RuhRagaUiState, yearMonth: YearMonth): RuhRagaUiState {
        val raw = prefs.getString("matrix_$yearMonth", null) ?: return state
        return try {
            val array = JSONArray(raw)
            val loadedMap = mutableMapOf<Int, MatrixDayCell>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val day = obj.getInt("day")
                val status = runCatching {
                    DayMatrixStatus.valueOf(obj.getString("status"))
                }.getOrDefault(DayMatrixStatus.REST)
                loadedMap[day] = MatrixDayCell(
                    day = day,
                    status = status,
                    ruhPercent = obj.optInt("ruhPercent", 0),
                    ragaPercent = obj.optInt("ragaPercent", 0),
                    completionPercent = obj.optInt("completionPercent", 0),
                    summary = obj.optString("summary", "Belum ada aktivitas tercatat")
                )
            }
            val merged = state.matrixDays.map { cell -> loadedMap[cell.day] ?: cell }
            state.copy(matrixDays = merged)
        } catch (_: Exception) {
            state
        }
    }

    private fun restoreDailyLog(state: RuhRagaUiState, date: LocalDate): RuhRagaUiState {
        val raw = prefs.getString("daily_log_$date", null) ?: return state
        return try {
            val obj = JSONObject(raw)

            val prayerMap = mutableMapOf<String, Triple<Boolean, Boolean, String?>>()
            val prayersArr = obj.optJSONArray("prayers")
            if (prayersArr != null) {
                for (i in 0 until prayersArr.length()) {
                    val pObj = prayersArr.getJSONObject(i)
                    val completedAtRaw = pObj.optString("completedAt", "")
                    prayerMap[pObj.getString("id")] = Triple(
                        pObj.optBoolean("isCompleted", false),
                        pObj.optBoolean("isJamaah", false),
                        completedAtRaw.ifBlank { null }
                    )
                }
            }
            val restoredPrayers: List<PrayerItem> = state.prayers.map { p ->
                val saved = prayerMap[p.id]
                if (saved != null) {
                    p.copy(
                        isCompleted = saved.first,
                        isJamaah = saved.second,
                        completedAt = saved.third
                    )
                } else p
            }

            val sunnahMap = mutableMapOf<String, Boolean>()
            val sunnahArr = obj.optJSONArray("sunnah")
            if (sunnahArr != null) {
                for (i in 0 until sunnahArr.length()) {
                    val sObj = sunnahArr.getJSONObject(i)
                    sunnahMap[sObj.getString("id")] = sObj.optBoolean("isCompleted", false)
                }
            }
            val restoredSunnah: List<SunnahItem> = state.sunnahItems.map { s ->
                val saved = sunnahMap[s.id]
                if (saved != null) s.copy(isCompleted = saved) else s
            }

            val tilawahObj = obj.optJSONObject("tilawah")
            val restoredTilawah: TilawahState = if (tilawahObj != null) {
                state.tilawah.copy(
                    isCompletedToday = tilawahObj.optBoolean("isCompletedToday", false),
                    progressPercent = tilawahObj.optInt("progressPercent", 0),
                    currentPage = tilawahObj.optInt("currentPage", 0)
                )
            } else state.tilawah

            val tasbihObj = obj.optJSONObject("tasbih")
            val restoredTasbih: TasbihState = if (tasbihObj != null) {
                state.tasbih.copy(
                    subhanallah = tasbihObj.optInt("subhanallah", 0),
                    alhamdulillah = tasbihObj.optInt("alhamdulillah", 0),
                    allahuAkbar = tasbihObj.optInt("allahuAkbar", 0)
                )
            } else state.tasbih

            val exercise = runCatching {
                ExerciseCategory.valueOf(obj.optString("selectedExercise", state.selectedExercise.name))
            }.getOrDefault(state.selectedExercise)

            val cardioMode = runCatching {
                CardioMode.valueOf(obj.optString("cardioMode", state.cardioMode.name))
            }.getOrDefault(state.cardioMode)

            val restoredSets = mutableListOf<WorkoutSetItem>()
            val setsArr = obj.optJSONArray("completedSets")
            if (setsArr != null) {
                for (i in 0 until setsArr.length()) {
                    val setObj = setsArr.getJSONObject(i)
                    restoredSets.add(
                        WorkoutSetItem(
                            setNumber = setObj.getInt("setNumber"),
                            reps = setObj.getInt("reps"),
                            restFormatted = setObj.getString("restFormatted"),
                            isDone = setObj.optBoolean("isDone", true)
                        )
                    )
                }
            }

            val dailyPlanMap = mutableMapOf<String, Triple<Int, Int, Boolean>>()
            val planArr = obj.optJSONArray("workoutPlanDaily")
            if (planArr != null) {
                for (i in 0 until planArr.length()) {
                    val itemObj = planArr.getJSONObject(i)
                    dailyPlanMap[itemObj.getString("id")] = Triple(
                        itemObj.optInt("sets", 3),
                        itemObj.optInt("repsPerSet", 15),
                        itemObj.optBoolean("isCompletedToday", false)
                    )
                }
            }
            val restoredPlan = state.workoutPlan.map { item ->
                val daily = dailyPlanMap[item.id]
                if (daily != null) {
                    item.copy(
                        sets = daily.first,
                        repsPerSet = daily.second,
                        isCompletedToday = daily.third
                    )
                } else item
            }

            state.copy(
                prayers = restoredPrayers,
                sunnahItems = restoredSunnah,
                tilawah = restoredTilawah,
                tasbih = restoredTasbih,
                workoutPlan = restoredPlan,
                selectedExercise = exercise,
                totalSessionTargetReps = exercise.targetReps,
                activeTimerMovement = exercise.label,
                currentExerciseReps = obj.optInt("currentExerciseReps", 0),
                totalSessionReps = obj.optInt("totalSessionReps", 0),
                currentSetNumber = obj.optInt("currentSetNumber", 1),
                activeTimerReps = obj.optInt("activeTimerReps", 0),
                completedSets = restoredSets,
                cardioMode = cardioMode,
                cardioDistanceKm = obj.optDouble("cardioDistanceKm", 0.0).toFloat(),
                cardioDurationMin = obj.optInt("cardioDurationMin", 0),
                cardioCalories = obj.optInt("cardioCalories", 0)
            )
        } catch (_: Exception) {
            state
        }
    }
}
