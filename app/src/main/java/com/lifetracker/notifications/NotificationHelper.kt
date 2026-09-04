package com.lifetracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lifetracker.MainActivity
import com.lifetracker.R

object NotificationHelper {

    // Channel IDs
    const val CHANNEL_HYDRATION = "channel_hydration"
    const val CHANNEL_STUDY = "channel_study"
    const val CHANNEL_GENERAL = "channel_general"

    // Notification IDs
    private const val NOTIF_HYDRATION = 1001
    private const val NOTIF_STUDY = 1002
    private const val NOTIF_GENERAL = 1003

    /**
     * Create all notification channels (required for Android 8+)
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Hydration channel
            NotificationChannel(
                CHANNEL_HYDRATION,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water throughout the day"
                enableVibration(true)
                manager.createNotificationChannel(this)
            }

            // Study channel
            NotificationChannel(
                CHANNEL_STUDY,
                "Study Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for your study sessions"
                enableVibration(true)
                manager.createNotificationChannel(this)
            }

            // General channel
            NotificationChannel(
                CHANNEL_GENERAL,
                "General Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General LifeTracker reminders"
                manager.createNotificationChannel(this)
            }
        }
    }

    /**
     * Show a hydration reminder notification
     */
    fun showHydrationReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "hydration")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messages = listOf(
            "Time to hydrate! 💧 Drink a glass of water.",
            "Stay hydrated! Your body needs water. 💧",
            "Water break! 💧 Keep up with your daily goal.",
            "Don't forget to drink water! 💧",
            "Hydration check! 💧 How much have you drunk today?"
        )
        val message = messages.random()

        val notification = NotificationCompat.Builder(context, CHANNEL_HYDRATION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Hydration Reminder 💧")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_HYDRATION, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Show a study reminder notification
     */
    fun showStudyReminder(context: Context, subject: String = "") {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = if (subject.isNotEmpty()) {
            "Time to study $subject! 📚"
        } else {
            "Time for your study session! 📚"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_STUDY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Study Reminder 🎓")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_STUDY, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Show a general reminder notification
     */
    fun showGeneralReminder(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_GENERAL, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
