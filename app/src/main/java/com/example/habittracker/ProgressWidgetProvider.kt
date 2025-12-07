package com.example.habittracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.habittracker.data.database.AppDatabase
import com.example.habittracker.data.repository.HabitRepository
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class ProgressWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // When first widget is created
    }

    override fun onDisabled(context: Context) {
        // When the last widget is removed
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_REFRESH -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, ProgressWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
            Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, ProgressWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        runBlocking {
            try {
                val database = AppDatabase.getInstance(context)
                val repository = HabitRepository(database.habitDao())

                val (completed, total) = repository.getTodayProgress()
                val progress = if (total == 0) 0 else (completed * 100) / total

                val views = RemoteViews(context.packageName, R.layout.widget_progress)

                val emoji = when {
                    progress == 100 -> "🎉"
                    progress >= 75 -> "😊"
                    progress >= 50 -> "🙂"
                    progress >= 25 -> "😐"
                    else -> "😔"
                }
                views.setTextViewText(R.id.txtWidgetProgress, "Today: $progress% $emoji")

                val refreshIntent = Intent(context, ProgressWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    refreshIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.btnRefresh, refreshPendingIntent)

                val appIntent = Intent(context, MainActivity::class.java)
                val appPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    appIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(android.R.id.background, appPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                // Fallback if there's an error
                val views = RemoteViews(context.packageName, R.layout.widget_progress)
                views.setTextViewText(R.id.txtWidgetProgress, "Today: Loading...")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.habittracker.widget.REFRESH"

        fun triggerUpdate(context: Context) {
            val intent = Intent(context, ProgressWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}