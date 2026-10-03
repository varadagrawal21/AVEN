package com.lifetracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lifetracker.MainActivity
import com.lifetracker.R

object NotificationHelper {

    const val CHANNEL_HYDRATION = "channel_hydration"
    const val CHANNEL_STUDY = "channel_study"
    const val CHANNEL_FINANCE = "channel_finance"
    const val CHANNEL_GENERAL = "channel_general"

    private const val NOTIF_HYDRATION = 1001
    private const val NOTIF_STUDY = 1002
    private const val NOTIF_FINANCE = 1003
    private const val NOTIF_GENERAL = 1004
    
    private const val NOTIF_TEST = 9999
    
    private const val TAG = "NotificationHelper"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "createNotificationChannels: creating notification channels")
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            NotificationChannel(
                CHANNEL_HYDRATION,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water throughout the day"
                enableVibration(true)
                manager.createNotificationChannel(this)
            }
            NotificationChannel(
                CHANNEL_STUDY,
                "Study Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for your study sessions"
                enableVibration(true)
                manager.createNotificationChannel(this)
            }
            NotificationChannel(
                CHANNEL_FINANCE,
                "Finance Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to review your finances"
                enableVibration(true)
                manager.createNotificationChannel(this)
            }
            NotificationChannel(
                CHANNEL_GENERAL,
                "General Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General LifeTracker reminders"
                manager.createNotificationChannel(this)
            }
            Log.d(TAG, "createNotificationChannels: channels created successfully")
        }
    }

    fun showReminder(context: Context, module: String, customMessage: String = "") {
        Log.d(TAG, "showReminder: module=$module")
        when (module) {
            "HYDRATION" -> showHydrationReminder(context, customMessage)
            "STUDY" -> showStudyReminder(context)
            "FINANCE" -> showFinanceReminder(context, customMessage)
            "GENERAL" -> showGeneralReminder(
                context,
                "LifeTracker Reminder",
                customMessage.ifBlank { "Time to check your LifeTracker progress." }
            )
            else -> {
                Log.w(TAG, "showReminder: unknown module $module, using general")
                showGeneralReminder(context, module, customMessage.ifBlank { "Reminder from LifeTracker." })
            }
        }
    }
    
    fun showTestNotification(context: Context) {
        Log.d(TAG, "showTestNotification: sending test notification")
        showNotification(
            context = context,
            notificationId = NOTIF_TEST,
            channel = CHANNEL_GENERAL,
            title = "Test Notification",
            message = "This is a test notification from LifeTracker. If you see this, notifications are working!",
            destination = "settings"
        )
    }
    
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun showHydrationReminder(context: Context, customMessage: String = "") {
        val message = customMessage.ifBlank {
            listOf(
                "Time to hydrate! Drink a glass of water.",
                "Stay hydrated! Your body needs water.",
                "Water break! Keep up with your daily goal.",
                "Do not forget to drink water.",
                "Hydration check! How much have you drunk today?"
            ).random()
        }
        showNotification(
            context = context,
            notificationId = NOTIF_HYDRATION,
            channel = CHANNEL_HYDRATION,
            title = "Hydration Reminder",
            message = message,
            destination = "hydration"
        )
    }

    fun showStudyReminder(context: Context, subject: String = "") {
        val message = if (subject.isNotEmpty()) {
            "Time to study $subject!"
        } else {
            "Time for your study session!"
        }
        showNotification(
            context = context,
            notificationId = NOTIF_STUDY,
            channel = CHANNEL_STUDY,
            title = "Study Reminder",
            message = message,
            destination = "academic"
        )
    }

    fun showFinanceReminder(context: Context, customMessage: String = "") {
        showNotification(
            context = context,
            notificationId = NOTIF_FINANCE,
            channel = CHANNEL_FINANCE,
            title = "Finance Reminder",
            message = customMessage.ifBlank { "Time to review your income and expenses." },
            destination = "finance"
        )
    }

    fun showGeneralReminder(context: Context, title: String, message: String) {
        showNotification(
            context = context,
            notificationId = NOTIF_GENERAL,
            channel = CHANNEL_GENERAL,
            title = title,
            message = message,
            destination = "dashboard"
        )
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        channel: String,
        title: String,
        message: String,
        destination: String
    ) {
        Log.d(TAG, "showNotification: id=$notificationId channel=$channel title=$title")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", destination)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.d(TAG, "showNotification: notification $notificationId posted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "showNotification: failed to post notification $notificationId", e)
        }
    }
}
