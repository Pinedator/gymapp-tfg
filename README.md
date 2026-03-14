# GymApp 💪

Android fitness tracking app with RPG gamification system. Built as a final project for the DAM (Multiplatform Application Development) vocational degree at CEAC FP.

## Description

GymApp is a native Android application that combines workout tracking with an RPG-style progression system. Users can create and manage custom routines, complete workouts to earn XP, and watch their character evolve as they level up.

## Features

- 📋 Create, edit and delete custom workout routines
- ⚡ Complete routines to earn XP and level up
- 👤 RPG character that evolves with your progress
- 📊 Strength and consistency stats tracking
- 📜 Workout history with XP gained per session
- 💾 Local data persistence with SQLite

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Database:** SQLite (SQLiteOpenHelper)
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 35 (Android 15)

## Gamification System

XP is calculated based on workout volume:
```
XP = Σ (sets × reps × weight) / 300
```
Characters evolve through three ranks:
- 🥊 **NOVATO** — Level 1-4
- ⚔️ **GUERRERO** — Level 5-9
- 👑 **LEYENDA** — Level 10+

## Project Structure
```
app/src/main/java/com/example/gymapp/
├── MainActivity.kt        # UI, composables and business logic
├── DatabaseHelper.kt      # SQLite data layer
└── ui/theme/
    ├── Color.kt           # Gaming color palette
    ├── Theme.kt           # App theme
    └── Type.kt            # Typography
```

## Screenshots

| Perfil | Rutinas | Historial |
|--------|---------|-----------|
| ![Perfil](screenshots/perfil.png) | ![Rutinas](screenshots/rutinas.png) | ![Historial](screenshots/historial.png) |

## Author

**Sergio Pineda Cuadrado**  
DAM — CEAC FP  
Tutor: Javier González Saturde  
Course: 2025/2026

## License

This project was developed for academic purposes.
