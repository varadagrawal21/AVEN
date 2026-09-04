package com.lifetracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver that reschedules WorkManager jobs after device reboot.
 * WorkManager persists jobs across reboots by default, but this receiver
 * ensures any AlarmManager-based exact alarms are also rescheduled.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // Reschedule hydration reminders
            // The actual scheduling is driven by user preferences stored in DataStore/Room
            // WorkManager jobs survive reboots automatically, but we trigger a re-enqueue
            // to be safe
            HydrationReminderWorker.schedule(context)
        }
    }
}
