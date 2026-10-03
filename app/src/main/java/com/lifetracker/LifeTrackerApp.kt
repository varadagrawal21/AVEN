package com.lifetracker

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.lifetracker.notifications.NotificationHelper
import com.lifetracker.notifications.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LifeTrackerApp : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "LifeTrackerApp"
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: initializing app")
        NotificationHelper.createNotificationChannels(this)
        Log.d(TAG, "onCreate: notification channels created")
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "onCreate: rescheduling all reminders")
            ReminderScheduler.rescheduleAll(this@LifeTrackerApp)
            Log.d(TAG, "onCreate: reschedule completed")
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
