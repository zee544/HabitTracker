package com.example.habittracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var calendar: CalendarView
    private lateinit var addMoodBtn: Button
    private lateinit var tvMoodList: TextView

    private var selectedDayKey: String? = null

    private lateinit var btnShare: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        val view = inflater.inflate(R.layout.fragment_mood, container, false)


        calendar = view.findViewById(R.id.calendarMoods)
        addMoodBtn = view.findViewById(R.id.btnAddMood)
        tvMoodList = view.findViewById(R.id.tvMoodList)
        btnShare = view.findViewById(R.id.btnShare)

        // Handle calendar selection
        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDayKey = String.format("%04d%02d%02d", year, month + 1, dayOfMonth)
            refreshMoods()
        }

        // Add Mood button
        addMoodBtn.setOnClickListener {
            pickMood()
        }
        btnShare.setOnClickListener { shareSummary() }

        refreshMoods()

        return view
    }

    private fun pickMood() {
        val moodOptions = arrayOf(
            "😀 Feeling extremely happy and full of energy",
            "🙂 Feeling calm, peaceful, and satisfied",
            "😐 Emotionally balanced or indifferent",
            "😕 Feeling puzzled or uncertain",
            "😢 Feeling sorrow or disappointment",
            "😡 Feeling frustration or strong displeasure",
            "🤗 Feeling cared for and connected",
            "🤩 Feeling enthusiastic and energetic",
            "😴 Feeling sleepy or drained"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Mood")
            .setItems(moodOptions) { _, which ->
                val selected = moodOptions[which]
                Prefs.addMood(requireContext(), System.currentTimeMillis(), selected)
                refreshMoods()
            }
            .show()
    }

    private fun refreshMoods() {
        val moods = Prefs.getMoods(requireContext())

        val sdfDate = SimpleDateFormat("h:mm a", Locale.getDefault())
        val sdfKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        val filtered = moods.filter { (ts, _) ->
            selectedDayKey == null || sdfKey.format(Date(ts)) == selectedDayKey
        }

        tvMoodList.text = if (filtered.isEmpty()) {
            "No mood entries yet"
        } else {
            filtered.joinToString("\n") { (ts, moodText) ->
                "${sdfDate.format(Date(ts))} - $moodText"
            }
        }
    }

    private fun shareSummary() {
        val recent = Prefs.getMoods(requireContext()).take(7)
        if (recent.isEmpty()) return
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        val text = recent.joinToString("\n") { (ts, emoji) ->
            sdf.format(Date(ts)) + ": " + emoji
        }
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "My recent moods:\n" + text)
        startActivity(android.content.Intent.createChooser(intent, "Share mood summary"))
    }
}
