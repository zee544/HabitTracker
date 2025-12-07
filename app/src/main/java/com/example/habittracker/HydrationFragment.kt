package com.example.habittracker

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class HydrationFragment : Fragment() {

    private lateinit var switchReminder: Switch
    private lateinit var textInterval: TextView
    private lateinit var seekBarInterval: SeekBar
    private lateinit var btnAddWater: Button
    private lateinit var btnRemoveWater: Button
    private lateinit var textWaterCount: TextView
    private lateinit var textGoalProgress: TextView

    // Profile
    private lateinit var textUserName: TextView
    private lateinit var textUserEmail: TextView
    private lateinit var textUserAge: TextView
    private lateinit var btnEditProfile: Button

    private val todayKey: String by lazy {
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hydration, container, false)

        //  hydration views
        switchReminder = view.findViewById(R.id.switchReminder)
        textInterval = view.findViewById(R.id.textInterval)
        seekBarInterval = view.findViewById(R.id.seekBarInterval)
        btnAddWater = view.findViewById(R.id.btnAddWater)
        btnRemoveWater = view.findViewById(R.id.btnRemoveWater)
        textWaterCount = view.findViewById(R.id.textWaterCount)
        textGoalProgress = view.findViewById(R.id.textGoalProgress)

        // Profile section
        textUserName = view.findViewById(R.id.textUserName)
        textUserEmail = view.findViewById(R.id.textUserEmail)
        textUserAge = view.findViewById(R.id.textUserAge)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)

        setupHydrationSettings()
        setupWaterTracker()
        setupProfileSection()

        return view
    }

    // Hydration
    private fun setupHydrationSettings() {
        switchReminder.isChecked = Prefs.isHydrationEnabled(requireContext())
        val currentMinutes = Prefs.getReminderMinutes(requireContext())
        seekBarInterval.progress = if (currentMinutes > 0) (currentMinutes / 5) - 1 else 1
        updateIntervalText()

        //enable or disable
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            Prefs.setHydrationEnabled(requireContext(), isChecked)
            if (isChecked) {
                HydrationReminder.scheduleReminder(requireContext())
                Toast.makeText(requireContext(), "Hydration reminders enabled", Toast.LENGTH_SHORT).show()
            } else {
                HydrationReminder.cancelReminder(requireContext())
                Toast.makeText(requireContext(), "Hydration reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }
         //moves slider 5,10..
        seekBarInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) updateIntervalText()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val minutes = (seekBarInterval.progress + 1) * 5
                Prefs.setReminderMinutes(requireContext(), minutes)
                if (switchReminder.isChecked) {
                    HydrationReminder.scheduleReminder(requireContext())
                }
            }
        })
    }

    private fun updateIntervalText() {
        val minutes = (seekBarInterval.progress + 1) * 5
        textInterval.text = "Remind every $minutes minutes"
    }

    private fun setupWaterTracker() {
        updateWaterDisplay()

        btnAddWater.setOnClickListener {
            Prefs.incrementWater(requireContext(), todayKey)
            updateWaterDisplay()
            Toast.makeText(requireContext(), "➕ Glass of water added", Toast.LENGTH_SHORT).show()
        }

        btnRemoveWater.setOnClickListener {
            Prefs.decrementWater(requireContext(), todayKey)
            updateWaterDisplay()
            Toast.makeText(requireContext(), "➖ Glass of water removed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWaterDisplay() {
        val waterCount = Prefs.getWaterCount(requireContext(), todayKey)
        textWaterCount.text = "Today: $waterCount glasses"

        val goal = 8
        val progress = (waterCount * 100 / goal).coerceAtMost(100)
        textGoalProgress.text = "Goal: $waterCount/8 glasses ($progress%)"
    }

    // Profile
    private fun setupProfileSection() {
        loadProfileDetails()

        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun loadProfileDetails() {
        textUserName.text = "Name: ${Prefs.getUserName(requireContext()) ?: "John Doe"}"
        textUserEmail.text = "Email: ${Prefs.getUserEmail(requireContext()) ?: "johndoe@example.com"}"
        textUserAge.text = "Age: ${Prefs.getUserAge(requireContext()) ?: "25"}"
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)

        val editName = dialogView.findViewById<EditText>(R.id.editName)
        val editEmail = dialogView.findViewById<EditText>(R.id.editEmail)
        val editAge = dialogView.findViewById<EditText>(R.id.editAge)

        // Pre-fill current details
        editName.setText(Prefs.getUserName(requireContext()) ?: "")
        editEmail.setText(Prefs.getUserEmail(requireContext()) ?: "")
        editAge.setText(Prefs.getUserAge(requireContext()) ?: "")

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = editName.text.toString().trim()
                val email = editEmail.text.toString().trim()
                val age = editAge.text.toString().trim()

                if (name.isNotEmpty()) Prefs.setUserName(requireContext(), name)
                if (email.isNotEmpty()) Prefs.setUserEmail(requireContext(), email)
                if (age.isNotEmpty()) Prefs.setUserAge(requireContext(), age)

                loadProfileDetails()
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}
