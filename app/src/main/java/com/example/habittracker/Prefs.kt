package com.example.habittracker

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
object Prefs {
    private const val PREFS_NAME = "wellness_prefs"
    private const val USER_PREFS_NAME = "prefs" // for user details

    private const val KEY_MOODS = "moods_list"
    private const val KEY_HABITS = "habits_list"
    private const val KEY_COMPLETED_PREFIX = "completed_"
    private const val KEY_REMINDER_MINUTES = "reminder_minutes"
    private const val KEY_WATER_PREFIX = "water_"
    private const val KEY_HYDRATION_ENABLED = "hydration_enabled"
    private const val KEY_MOOD_ENTRIES = "mood_entries"

    //  User Profile
    private fun userPrefs(context: Context) =
        context.getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE)

    fun getUserName(context: Context): String? =
        userPrefs(context).getString("user_name", null)

    fun setUserName(context: Context, name: String) =
        userPrefs(context).edit().putString("user_name", name).apply()

    fun getUserEmail(context: Context): String? =
        userPrefs(context).getString("user_email", null)

    fun setUserEmail(context: Context, email: String) =
        userPrefs(context).edit().putString("user_email", email).apply()

    fun getUserAge(context: Context): String? =
        userPrefs(context).getString("user_age", null)

    fun setUserAge(context: Context, age: String) =
        userPrefs(context).edit().putString("user_age", age).apply()

    // Mood Tracking
    fun addMood(context: Context, timestamp: Long, moodText: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val list = prefs.getStringSet(KEY_MOODS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        list.add("$timestamp|$moodText")
        prefs.edit().putStringSet(KEY_MOODS, list).apply()
    }

    fun getMoods(context: Context): List<Pair<Long, String>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val list = prefs.getStringSet(KEY_MOODS, emptySet()) ?: emptySet()
        return list.mapNotNull {
            val parts = it.split("|", limit = 2)
            if (parts.size == 2) parts[0].toLongOrNull()?.let { ts -> ts to parts[1] } else null
        }.sortedByDescending { it.first }
    }

    // Habit Tracking
    fun getHabits(context: Context): List<Pair<String, String>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val list = prefs.getStringSet(KEY_HABITS, emptySet()) ?: emptySet()
        return list.mapNotNull {
            val parts = it.split("|", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
    }

    fun saveHabits(context: Context, habits: List<Pair<String, String>>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = habits.map { "${it.first}|${it.second}" }.toSet()
        prefs.edit().putStringSet(KEY_HABITS, set).apply()
    }

    fun getTodayCompleted(context: Context, dateKey: String): MutableSet<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet("$KEY_COMPLETED_PREFIX$dateKey", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    }

    fun setTodayCompleted(context: Context, dateKey: String, completed: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet("$KEY_COMPLETED_PREFIX$dateKey", completed).apply()
    }

    fun getCompletedForDate(context: Context, dateKey: String): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet("$KEY_COMPLETED_PREFIX$dateKey", emptySet()) ?: emptySet()
    }

    //Hydration
    fun setHydrationEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_HYDRATION_ENABLED, enabled).apply()
    }

    fun isHydrationEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HYDRATION_ENABLED, false)
    }

    fun setReminderMinutes(context: Context, minutes: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_REMINDER_MINUTES, minutes).apply()
    }

    fun getReminderMinutes(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_REMINDER_MINUTES, 0)
    }

    fun getWaterCount(context: Context, dateKey: String): Int {
        val key = KEY_WATER_PREFIX + dateKey
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(key, 0)
    }

    fun incrementWater(context: Context, dateKey: String) {
        val key = KEY_WATER_PREFIX + dateKey
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    fun decrementWater(context: Context, dateKey: String) {
        val key = KEY_WATER_PREFIX + dateKey
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(key, 0)
        prefs.edit().putInt(key, (count - 1).coerceAtLeast(0)).apply()
    }

    fun getLastNDaysWaterCounts(context: Context, days: Int, dateKeys: List<String>): List<Int> {
        return dateKeys.map { getWaterCount(context, it) }
    }
    fun getTodayProgress(context: Context): Int {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayKey = sdf.format(Date())
        val habits = getHabits(context)
        val completed = getTodayCompleted(context, todayKey)

        return if (habits.isEmpty()) 0 else (completed.size * 100) / habits.size
    }

    fun getProgressForDate(context: Context, dateKey: String): Int {
        val habits = getHabits(context)
        val completed = getCompletedForDate(context, dateKey)

        return if (habits.isEmpty()) 0 else (completed.size * 100) / habits.size
    }

    fun getProgressEmoji(progress: Int): String {
        return when {
            progress == 100 -> "🎉"
            progress >= 75 -> "😊"
            progress >= 50 -> "🙂"
            progress >= 25 -> "😐"
            else -> "😔"
        }
    }

    // Helper to get today's date key
    fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date())
    }
}
