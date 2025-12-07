package com.example.habittracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object HydrationReminder {

    private const val REQUEST_CODE = 123

    fun scheduleReminder(context: Context) {
        val minutes = Prefs.getReminderMinutes(context)
        if (minutes <= 0 || !Prefs.isHydrationEnabled(context)) {
            cancelReminder(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Start from now, repeat every  minutes
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            (minutes * 60 * 1000).toLong(),
            pendingIntent
        )
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}