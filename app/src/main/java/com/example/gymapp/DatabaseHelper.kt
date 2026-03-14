package com.example.gymapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "gymapp.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // Tabla usuario (una sola fila con id=1)
        db.execSQL("""
            CREATE TABLE user (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                level INTEGER NOT NULL,
                xp INTEGER NOT NULL,
                strength INTEGER NOT NULL,
                consistency INTEGER NOT NULL
            )
        """)

        // Tabla rutinas
        db.execSQL("""
            CREATE TABLE routines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
        """)

        // Tabla ejercicios (ligados a una rutina)
        db.execSQL("""
            CREATE TABLE exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                routine_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                sets INTEGER NOT NULL,
                reps INTEGER NOT NULL,
                weight REAL NOT NULL,
                FOREIGN KEY(routine_id) REFERENCES routines(id) ON DELETE CASCADE
            )
        """)

        // Tabla historial
        db.execSQL("""
            CREATE TABLE history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                routine_name TEXT NOT NULL,
                xp_gained INTEGER NOT NULL
            )
        """)

        // Tabla ejercicios del historial
        db.execSQL("""
            CREATE TABLE history_exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                history_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                sets INTEGER NOT NULL,
                reps INTEGER NOT NULL,
                weight REAL NOT NULL,
                FOREIGN KEY(history_id) REFERENCES history(id) ON DELETE CASCADE
            )
        """)

        // Usuario inicial
        db.execSQL("""
            INSERT INTO user (id, name, level, xp, strength, consistency)
            VALUES (1, 'Usuario', 1, 0, 0, 0)
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS history_exercises")
        db.execSQL("DROP TABLE IF EXISTS history")
        db.execSQL("DROP TABLE IF EXISTS exercises")
        db.execSQL("DROP TABLE IF EXISTS routines")
        db.execSQL("DROP TABLE IF EXISTS user")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Activar claves foráneas en cada conexión
        db.execSQL("PRAGMA foreign_keys=ON")
    }

    // ──────────────────────────────────────────────
    // USER
    // ──────────────────────────────────────────────

    fun getUser(): User {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM user WHERE id = 1", null)
        cursor.moveToFirst()
        val user = User(
            name        = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            level       = cursor.getInt(cursor.getColumnIndexOrThrow("level")),
            xp          = cursor.getInt(cursor.getColumnIndexOrThrow("xp")),
            stats       = Stats(
                strength    = cursor.getInt(cursor.getColumnIndexOrThrow("strength")),
                consistency = cursor.getInt(cursor.getColumnIndexOrThrow("consistency"))
            )
        )
        cursor.close()
        return user
    }

    fun updateUser(user: User) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name",        user.name)
            put("level",       user.level)
            put("xp",          user.xp)
            put("strength",    user.stats.strength)
            put("consistency", user.stats.consistency)
        }
        db.update("user", values, "id = 1", null)
    }

    // ──────────────────────────────────────────────
    // ROUTINES
    // ──────────────────────────────────────────────

    fun getAllRoutines(): List<Rutina> {
        val db = readableDatabase
        val rutinas = mutableListOf<Rutina>()

        val cursor = db.rawQuery("SELECT * FROM routines", null)
        while (cursor.moveToNext()) {
            val id   = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            rutinas.add(Rutina(id = id, name = name, ejercicios = getExercisesForRoutine(id)))
        }
        cursor.close()
        return rutinas
    }

    fun insertRoutine(rutina: Rutina): Long {
        val db = writableDatabase
        val values = ContentValues().apply { put("name", rutina.name) }
        val routineId = db.insert("routines", null, values)
        rutina.ejercicios.forEach { insertExercise(it, routineId.toInt()) }
        return routineId
    }

    fun updateRoutine(rutina: Rutina) {
        val db = writableDatabase
        val values = ContentValues().apply { put("name", rutina.name) }
        db.update("routines", values, "id = ?", arrayOf(rutina.id.toString()))
        // Borrar ejercicios anteriores y reinsertar
        db.delete("exercises", "routine_id = ?", arrayOf(rutina.id.toString()))
        rutina.ejercicios.forEach { insertExercise(it, rutina.id) }
    }

    fun deleteRoutine(routineId: Int) {
        writableDatabase.delete("routines", "id = ?", arrayOf(routineId.toString()))
        // Los ejercicios se borran solos por ON DELETE CASCADE
    }

    // ──────────────────────────────────────────────
    // EXERCISES (internas, usadas por Routine)
    // ──────────────────────────────────────────────

    private fun insertExercise(ejercicio: Ejercicio, routineId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("routine_id", routineId)
            put("name",       ejercicio.name)
            put("sets",       ejercicio.sets)
            put("reps",       ejercicio.reps)
            put("weight",     ejercicio.weight)
        }
        db.insert("exercises", null, values)
    }

    private fun getExercisesForRoutine(routineId: Int): List<Ejercicio> {
        val db = readableDatabase
        val list = mutableListOf<Ejercicio>()
        val cursor = db.rawQuery(
            "SELECT * FROM exercises WHERE routine_id = ?",
            arrayOf(routineId.toString())
        )
        while (cursor.moveToNext()) {
            list.add(
                Ejercicio(
                    name   = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    sets   = cursor.getInt(cursor.getColumnIndexOrThrow("sets")),
                    reps   = cursor.getInt(cursor.getColumnIndexOrThrow("reps")),
                    weight = cursor.getFloat(cursor.getColumnIndexOrThrow("weight"))
                )
            )
        }
        cursor.close()
        return list
    }

    // ──────────────────────────────────────────────
    // HISTORY
    // ──────────────────────────────────────────────

    fun getAllHistory(): List<Historial> {
        val db = readableDatabase
        val list = mutableListOf<Historial>()

        val cursor = db.rawQuery("SELECT * FROM history ORDER BY id DESC", null)
        while (cursor.moveToNext()) {
            val id          = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val routineName = cursor.getString(cursor.getColumnIndexOrThrow("routine_name"))
            val xpGained    = cursor.getInt(cursor.getColumnIndexOrThrow("xp_gained"))
            list.add(
                Historial(
                    routineName    = routineName,
                    exercisesDone  = getHistoryExercises(id),
                    xpGained       = xpGained
                )
            )
        }
        cursor.close()
        return list
    }

    fun insertHistory(history: Historial) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("routine_name", history.routineName)
            put("xp_gained",    history.xpGained)
        }
        val historyId = db.insert("history", null, values)
        history.exercisesDone.forEach { exercise ->
            val ev = ContentValues().apply {
                put("history_id", historyId)
                put("name",       exercise.name)
                put("sets",       exercise.sets)
                put("reps",       exercise.reps)
                put("weight",     exercise.weight)
            }
            db.insert("history_exercises", null, ev)
        }
    }

    private fun getHistoryExercises(historyId: Int): List<Ejercicio> {
        val db = readableDatabase
        val list = mutableListOf<Ejercicio>()
        val cursor = db.rawQuery(
            "SELECT * FROM history_exercises WHERE history_id = ?",
            arrayOf(historyId.toString())
        )
        while (cursor.moveToNext()) {
            list.add(
                Ejercicio(
                    name   = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    sets   = cursor.getInt(cursor.getColumnIndexOrThrow("sets")),
                    reps   = cursor.getInt(cursor.getColumnIndexOrThrow("reps")),
                    weight = cursor.getFloat(cursor.getColumnIndexOrThrow("weight"))
                )
            )
        }
        cursor.close()
        return list
    }
}


