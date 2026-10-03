package com.lifetracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: action=${intent.action}")
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            Log.d(TAG, "onReceive: ignoring action ${intent.action}")
            return
        }
        Log.d(TAG, "onReceive: rescheduling all reminders")
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ReminderScheduler.rescheduleAll(context)
                Log.d(TAG, "onReceive: reschedule completed")
            } catch (e: Exception) {
                Log.e(TAG, "onReceive: error rescheduling", e)
            } finally {
                pendingResult.finish()
                Log.d(TAG, "onReceive: finished")
            }
        }
    }
}
