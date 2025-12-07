package com.example.habittracker

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.data.database.AppDatabase
import com.example.habittracker.data.repository.HabitRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import java.text.SimpleDateFormat

import java.util.*

class HabitFragment : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvQuote: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtProgressValue: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnAddHabit: Button
    private lateinit var adapter: HabitsAdapter

    private lateinit var repository: HabitRepository
    private val todayKey: String by lazy {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        sdf.format(Date())
    }

    private val quotes = listOf(
        "Small steps every day create big changes 🌱",
        "Drink some water, your body will thank you 💧",
        "Take a deep breath and relax 🧘",
        "Consistency beats motivation every time 🚀",
        "A good mood is the best fuel for the day 😊",
        "Your health is your wealth 💙",
        "Stay positive, work hard, and make it happen ✨"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        // Initialize database
        val database = AppDatabase.getInstance(requireContext())
        repository = HabitRepository(database.habitDao())

        // Link the UI
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvQuote = view.findViewById(R.id.tvQuote)
        recyclerView = view.findViewById(R.id.recyclerHabits)
        txtProgressValue = view.findViewById(R.id.txtProgressValue)
        progressBar = view.findViewById(R.id.progressBar)
        btnAddHabit = view.findViewById(R.id.btnAddHabit)

        setGreeting()
        setRandomQuote()
        setupHabitsList()

        btnAddHabit.setOnClickListener { promptAddHabit() }

        return view
    }

    private fun setGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good Morning ☀️"
            in 12..17 -> "Good Afternoon 🌸"
            else -> "Good Evening 🌙"
        }
        tvGreeting.text = greeting
    }

    private fun setRandomQuote() {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val index = today.hashCode().absoluteValue % quotes.size
        tvQuote.text = quotes[index]
    }

    private fun setupHabitsList() {
        adapter = HabitsAdapter(
            items = mutableListOf(),
            onCheckedChanged = { position, checked ->
                lifecycleScope.launch {
                    val habit = adapter.getHabitAt(position)
                    if (checked) {
                        repository.markHabitCompleted(habit.id)
                    } else {
                        repository.unmarkHabitCompleted(habit.id)
                    }
                    updateProgress()
                    updateWidget()
                }
            },
            onEditClicked = { position ->
                promptEditHabit(position)
            },
            onDeleteClicked = { position ->
                showDeleteConfirmation(position)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadHabits()
        updateProgress()
    }

    private fun loadHabits() {
        lifecycleScope.launch {
            repository.getAllHabits().collect { habits ->
                val habitItems = habits.map { habit ->
                    val isCompleted = repository.isHabitCompletedToday(habit.id)
                    val streak = repository.calculateStreakDays(habit.id)
                    HabitItem(habit.id, habit.name, isCompleted, streak)
                }
                adapter.update(habitItems)
            }
        }
    }

    private fun updateProgress() {
        lifecycleScope.launch {
            val (completed, total) = repository.getTodayProgress()
            val progress = if (total == 0) 0 else (completed * 100) / total
            txtProgressValue.text = "$progress%"
            progressBar.progress = progress
        }
    }

    private fun promptAddHabit() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "Enter habit name"

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("New Habit")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        val id = UUID.randomUUID().toString()
                        repository.addHabit(id, name)
                        loadHabits()
                        updateProgress()
                        updateWidget()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun promptEditHabit(position: Int) {
        val habit = adapter.getHabitAt(position)
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(habit.name)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        val updatedHabit = habit.copy(name = name)
                        repository.updateHabit(updatedHabit)
                        loadHabits()
                        updateWidget()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(position: Int) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete this habit?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val habit = adapter.getHabitAt(position)
                    repository.deleteHabit(habit.id)
                    loadHabits()
                    updateProgress()
                    updateWidget()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateWidget() {
        (requireActivity() as? HomeActivity)?.updateWidget()
    }

    private val Int.absoluteValue: Int
        get() = if (this < 0) -this else this
}