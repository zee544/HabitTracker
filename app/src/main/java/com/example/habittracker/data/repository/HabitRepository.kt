package com.example.habittracker.data.repository

import com.example.habittracker.HabitItem
import com.example.habittracker.data.dao.HabitDao
import com.example.habittracker.data.entities.Habit
import com.example.habittracker.data.entities.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class HabitRepository(private val habitDao: HabitDao) {

    // Habit operations
    suspend fun addHabit(id: String, name: String) {
        val habit = Habit(id = id, name = name)
        habitDao.insertHabit(habit)
    }

    fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllActiveHabits()
    }

    suspend fun updateHabit(habit: HabitItem) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: String) {
        habitDao.deleteHabit(habitId)
    }

    // Completion operations
    suspend fun markHabitCompleted(habitId: String, date: String = getTodayDate()) {
        val existing = habitDao.getCompletion(habitId, date)
        if (existing == null) {
            habitDao.insertHabitCompletion(HabitCompletion(habitId = habitId, completionDate = date))
        }
    }

    suspend fun unmarkHabitCompleted(habitId: String, date: String = getTodayDate()) {
        habitDao.deleteCompletion(habitId, date)
    }

    suspend fun isHabitCompletedToday(habitId: String): Boolean {
        return habitDao.getCompletion(habitId, getTodayDate()) != null
    }

    // Progress calculations
    suspend fun getTodayProgress(): Pair<Int, Int> {
        val completed = habitDao.getCompletedCountForDate(getTodayDate())
        val total = habitDao.getTotalActiveHabitsCount()
        return Pair(completed, total)
    }

    suspend fun getCompletedHabitsForDate(date: String): List<String> {
        return habitDao.getCompletionsForDate(date).map { it.habitId }
    }

    // Streak calculation
    suspend fun calculateStreakDays(habitId: String): Int {
        val completionDates = habitDao.getCompletionDatesForHabit(habitId)
        val sortedDates = completionDates.sortedDescending()

        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        var streak = 0

        for (dateStr in sortedDates) {
            val date = sdf.parse(dateStr)
            calendar.time = date ?: continue

            // Check if dates are consecutive
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val previousDate = sdf.format(calendar.time)

            if (streak == 0 || sortedDates.contains(previousDate)) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }
}