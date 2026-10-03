package com.lifetracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.lifetracker.data.db.LifeTrackerDatabase
import com.lifetracker.data.db.NotificationPrefEntity
import kotlinx.coroutines.flow.first
import java.util.Calendar

object ReminderScheduler {
    const val ACTION_REMINDER = "com.lifetracker.action.REMINDER"
    const val EXTRA_MODULE = "module"
    
    private const val TAG = "ReminderScheduler"

    private const val REQUEST_HYDRATION = 1001
    private const val REQUEST_STUDY = 1002
    private const val REQUEST_FINANCE = 1003
    private const val REQUEST_GENERAL = 1004

    fun schedule(context: Context, module: String, pref: NotificationPrefEntity) {
        cancel(context, module)
        if (!pref.isEnabled) {
            Log.d(TAG, "schedule: $module is disabled, not scheduling")
            return
        }
        val triggerTime = calculateFirstTriggerTime(pref)
        Log.d(TAG, "schedule: $module first trigger at ${java.util.Date(triggerTime)} (in ${(triggerTime - System.currentTimeMillis()) / 60000} minutes)")
        setAlarm(context, module, pref, triggerTime)
    }

    fun scheduleNext(context: Context, module: String, pref: NotificationPrefEntity) {
        if (!pref.isEnabled) {
            Log.d(TAG, "scheduleNext: $module is disabled, cancelling")
            cancel(context, module)
            return
        }
        val intervalMs = intervalMillis(pref)
        val triggerTime = System.currentTimeMillis() + intervalMs
        Log.d(TAG, "scheduleNext: $module next trigger in ${intervalMs / 60000} minutes at ${java.util.Date(triggerTime)}")
        setAlarm(context, module, pref, triggerTime)
    }

    fun cancel(context: Context, module: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = pendingIntent(context, module)
        alarmManager.cancel(pending)
        Log.d(TAG, "cancel: cancelled alarm for $module")
    }

    suspend fun rescheduleAll(context: Context) {
        Log.d(TAG, "rescheduleAll: starting reschedule")
        val prefs = LifeTrackerDatabase.getInstance(context)
            .notificationPrefDao()
            .getAllPrefs()
            .first()
        prefs.forEach { pref ->
            if (pref.isEnabled) {
                Log.d(TAG, "rescheduleAll: rescheduling ${pref.module}")
                schedule(context, pref.module, pref)
            } else {
                cancel(context, pref.module)
            }
        }
        Log.d(TAG, "rescheduleAll: completed")
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return false
        return alarmManager.canScheduleExactAlarms()
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    private fun setAlarm(
        context: Context,
        module: String,
        pref: NotificationPrefEntity,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = pendingIntent(context, module)
        alarmManager.cancel(pendingIntent)

        val safeTriggerTime = maxOf(triggerAtMillis, System.currentTimeMillis() + 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.d(TAG, "setAlarm: $module using setAndAllowWhileIdle (no exact alarm permission)")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, safeTriggerTime, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "setAlarm: $module using setExactAndAllowWhileIdle")
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, safeTriggerTime, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.d(TAG, "setAlarm: $module using setExact")
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, safeTriggerTime, pendingIntent)
        } else {
            Log.d(TAG, "setAlarm: $module using set")
            alarmManager.set(AlarmManager.RTC_WAKEUP, safeTriggerTime, pendingIntent)
        }
    }

    private fun pendingIntent(context: Context, module: String): PendingIntent {
        val intent = Intent(ACTION_REMINDER, null, context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_MODULE, module)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCodeFor(module),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun calculateFirstTriggerTime(pref: NotificationPrefEntity): Long {
        val now = Calendar.getInstance()
        val startTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, pref.reminderHour.coerceIn(0, 23))
            set(Calendar.MINUTE, pref.reminderMinute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        if (startTime.after(now)) {
            Log.d(TAG, "calculateFirstTriggerTime: start time ${startTime.time} is in future, using it")
            return startTime.timeInMillis
        }
        
        val intervalMs = intervalMillis(pref)
        val elapsedSinceStart = now.timeInMillis - startTime.timeInMillis
        
        if (elapsedSinceStart <= 0) {
            return startTime.timeInMillis
        }
        
        val intervalsElapsed = elapsedSinceStart / intervalMs
        val nextTrigger = startTime.timeInMillis + ((intervalsElapsed + 1) * intervalMs)
        
        Log.d(TAG, "calculateFirstTriggerTime: using interval-based calculation, next trigger at ${java.util.Date(nextTrigger)}")
        return nextTrigger
    }

    private fun intervalMillis(pref: NotificationPrefEntity): Long {
        val hours = pref.intervalHours.coerceIn(1, 24).toLong()
        return hours * 60L * 60L * 1000L
    }

    private fun requestCodeFor(module: String): Int = when (module) {
        "HYDRATION" -> REQUEST_HYDRATION
        "STUDY" -> REQUEST_STUDY
        "FINANCE" -> REQUEST_FINANCE
        "GENERAL" -> REQUEST_GENERAL
        else -> module.hashCode()
    }
}
