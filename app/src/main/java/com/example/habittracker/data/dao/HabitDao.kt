package com.example.habittracker.data.dao

import androidx.room.*
import com.example.habittracker.HabitItem
import com.example.habittracker.data.entities.Habit
import com.example.habittracker.data.entities.HabitCompletion
import kotlinx.coroutines.flow.Flow


@Dao
interface HabitDao {

    // Habit operations
    @Insert
    suspend fun insertHabit(habit: Habit)

    @Query("SELECT * FROM habits WHERE is_active = 1 ORDER BY created_at DESC")
    fun getAllActiveHabits(): Flow<List<Habit>>

    @Update
    suspend fun updateHabit(habit: HabitItem)

    @Query("UPDATE habits SET is_active = 0 WHERE id = :habitId")
    suspend fun deleteHabit(habitId: String)

    // HabitCompletion operations
    @Insert
    suspend fun insertHabitCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habit_id = :habitId AND completion_date = :date")
    suspend fun deleteCompletion(habitId: String, date: String)

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId AND completion_date = :date")
    suspend fun getCompletion(habitId: String, date: String): HabitCompletion?

    @Query("SELECT * FROM habit_completions WHERE completion_date = :date")
    suspend fun getCompletionsForDate(date: String): List<HabitCompletion>

    // Progress calculations
    @Query("SELECT COUNT(*) FROM habit_completions WHERE completion_date = :date")
    suspend fun getCompletedCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM habits WHERE is_active = 1")
    suspend fun getTotalActiveHabitsCount(): Int

    // Streak calculation
    @Query("SELECT completion_date FROM habit_completions WHERE habit_id = :habitId ORDER BY completion_date DESC")
    suspend fun getCompletionDatesForHabit(habitId: String): List<String>
}