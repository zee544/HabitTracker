package com.example.habittracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Load HabitFragment by default
        if (savedInstanceState == null) {
            loadFragment(HabitFragment())
        }

        // Handle navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_habits -> loadFragment(HabitFragment())
                R.id.nav_mood -> loadFragment(MoodFragment())
                R.id.nav_settings -> loadFragment(HydrationFragment())
            }
            true
        }

        // Update widget when activity starts
        updateWidget()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Add this function to update widget
    fun updateWidget() {
        ProgressWidgetProvider.triggerUpdate(this)
    }
}