package com.ruhraga.tracker.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ruhraga.tracker.ui.state.CardioMode
import com.ruhraga.tracker.ui.state.ExerciseCategory
import com.ruhraga.tracker.ui.state.WorkoutHistoryEntry
import com.ruhraga.tracker.ui.state.WorkoutSetItem
import java.time.LocalDate

class WorkoutDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_WORKOUT_LOGS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                exercise TEXT NOT NULL,
                set_number INTEGER NOT NULL,
                reps INTEGER NOT NULL,
                rest_seconds INTEGER NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_workout_date ON $TABLE_WORKOUT_LOGS(date)")

        db.execSQL(
            """
            CREATE TABLE $TABLE_CARDIO_LOGS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                mode TEXT NOT NULL,
                distance_km REAL NOT NULL,
                duration_min INTEGER NOT NULL,
                calories INTEGER NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_cardio_date ON $TABLE_CARDIO_LOGS(date)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORKOUT_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CARDIO_LOGS")
        onCreate(db)
    }

    fun insertWorkoutEntry(
        date: LocalDate,
        exercise: ExerciseCategory,
        setNumber: Int,
        reps: Int,
        restSeconds: Int
    ) {
        val values = ContentValues().apply {
            put("date", date.toString())
            put("exercise", exercise.name)
            put("set_number", setNumber)
            put("reps", reps)
            put("rest_seconds", restSeconds)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insert(TABLE_WORKOUT_LOGS, null, values)
    }

    fun insertCardioEntry(
        date: LocalDate,
        mode: CardioMode,
        distanceKm: Float,
        durationMin: Int,
        calories: Int
    ) {
        val values = ContentValues().apply {
            put("date", date.toString())
            put("mode", mode.name)
            put("distance_km", distanceKm)
            put("duration_min", durationMin)
            put("calories", calories)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insert(TABLE_CARDIO_LOGS, null, values)
    }

    fun getExerciseRepsForDate(date: LocalDate, exercise: ExerciseCategory): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(reps), 0) FROM $TABLE_WORKOUT_LOGS WHERE date = ? AND exercise = ?",
            arrayOf(date.toString(), exercise.name)
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0).coerceAtLeast(0) else 0
        }
    }

    fun getTotalRepsForDate(date: LocalDate): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(reps), 0) FROM $TABLE_WORKOUT_LOGS WHERE date = ?",
            arrayOf(date.toString())
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0).coerceAtLeast(0) else 0
        }
    }

    fun getAllTimeTotalReps(): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(reps), 0) FROM $TABLE_WORKOUT_LOGS",
            null
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0).coerceAtLeast(0) else 0
        }
    }

    fun getSetsForDateAndExercise(date: LocalDate, exercise: ExerciseCategory): List<WorkoutSetItem> {
        val list = mutableListOf<WorkoutSetItem>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT set_number, reps, rest_seconds
            FROM $TABLE_WORKOUT_LOGS
            WHERE date = ? AND exercise = ? AND set_number > 0
            ORDER BY id ASC
            """.trimIndent(),
            arrayOf(date.toString(), exercise.name)
        )
        cursor.use {
            while (it.moveToNext()) {
                val setNum = it.getInt(0)
                val reps = it.getInt(1)
                val restSec = it.getInt(2)
                val restFormatted = "00:%02d rest".format(restSec.coerceIn(0, 59))
                list.add(
                    WorkoutSetItem(
                        setNumber = setNum,
                        reps = reps,
                        restFormatted = restFormatted,
                        isDone = true
                    )
                )
            }
        }
        return list
    }

    fun getRecentWorkoutHistory(limit: Int = 15): List<WorkoutHistoryEntry> {
        val entries = mutableListOf<WorkoutHistoryEntry>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT date, exercise, COALESCE(SUM(reps), 0) as total_reps, MAX(created_at) as last_ts
            FROM $TABLE_WORKOUT_LOGS
            GROUP BY date, exercise
            HAVING total_reps > 0
            ORDER BY last_ts DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(limit.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                val dateStr = it.getString(0)
                val exName = it.getString(1)
                val totalReps = it.getInt(2)
                val label = runCatching { ExerciseCategory.valueOf(exName).label }.getOrDefault(exName)
                entries.add(
                    WorkoutHistoryEntry(
                        date = dateStr,
                        title = label,
                        subtitle = "Calisthenics",
                        metricLabel = "$totalReps reps",
                        isCardio = false
                    )
                )
            }
        }

        val cardioCursor = readableDatabase.rawQuery(
            """
            SELECT date, mode, COALESCE(SUM(distance_km), 0), COALESCE(SUM(duration_min), 0), COALESCE(SUM(calories), 0)
            FROM $TABLE_CARDIO_LOGS
            GROUP BY date, mode
            ORDER BY MAX(created_at) DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(limit.toString())
        )
        cardioCursor.use {
            while (it.moveToNext()) {
                val dateStr = it.getString(0)
                val modeName = it.getString(1)
                val dist = it.getDouble(2)
                val dur = it.getInt(3)
                val kcal = it.getInt(4)
                val label = runCatching { CardioMode.valueOf(modeName).label }.getOrDefault(modeName)
                entries.add(
                    WorkoutHistoryEntry(
                        date = dateStr,
                        title = label,
                        subtitle = "${"%.1f".format(dist)} km • $dur min",
                        metricLabel = "$kcal kcal",
                        isCardio = true
                    )
                )
            }
        }

        return entries.sortedByDescending { it.date }.take(limit)
    }

    companion object {
        private const val DATABASE_NAME = "ruhraga_workout.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_WORKOUT_LOGS = "workout_logs"
        private const val TABLE_CARDIO_LOGS = "cardio_logs"
    }
}
