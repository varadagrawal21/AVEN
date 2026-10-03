package com.lifetracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.lifetracker.data.db.LifeTrackerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class ReminderReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "ReminderReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_REMINDER) {
            Log.d(TAG, "onReceive: ignoring unknown action ${intent.action}")
            return
        }
        val module = intent.getStringExtra(ReminderScheduler.EXTRA_MODULE) ?: return
        Log.d(TAG, "onReceive: received alarm for module=$module")
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pref = LifeTrackerDatabase.getInstance(context)
                    .notificationPrefDao()
                    .getPrefForModule(module)
                    .first()
                if (pref != null) {
                    Log.d(TAG, "onReceive: pref found for $module, isEnabled=${pref.isEnabled}")
                } else {
                    Log.w(TAG, "onReceive: no pref found for $module")
                }
                if (pref?.isEnabled == true) {
                    Log.d(TAG, "onReceive: showing notification for $module")
                    NotificationHelper.showReminder(context, module, pref.customMessage)
                    Log.d(TAG, "onReceive: scheduling next alarm for $module with interval=${pref.intervalHours}h")
                    ReminderScheduler.scheduleNext(context, module, pref)
                } else {
                    Log.d(TAG, "onReceive: $module is disabled, cancelling alarm")
                    ReminderScheduler.cancel(context, module)
                }
            } catch (e: Exception) {
                Log.e(TAG, "onReceive: error processing reminder for $module", e)
            } finally {
                Log.d(TAG, "onReceive: finishing async for $module")
                pendingResult.finish()
            }
        }
    }
}
