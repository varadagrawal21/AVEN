package com.lifetracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifetracker.data.db.NotificationPrefEntity
import com.lifetracker.data.db.UserEntity
import com.lifetracker.data.repository.UserRepository
import com.lifetracker.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val user: UserEntity? = null,
    val notificationPrefs: List<NotificationPrefEntity> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepo: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userRepo.getUser().collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
        viewModelScope.launch {
            userRepo.getAllNotificationPrefs().collect { prefs ->
                _state.update { it.copy(notificationPrefs = prefs) }
            }
        }
    }

    fun saveUserProfile(name: String, weightKg: Float, heightCm: Float, ageYears: Int, calorieGoal: Int) {
        viewModelScope.launch {
            val current = _state.value.user ?: UserEntity()
            val waterGoal = (weightKg * 35).toInt()
            userRepo.saveUser(
                current.copy(
                    name = name,
                    weightKg = weightKg,
                    heightCm = heightCm,
                    ageYears = ageYears,
                    dailyCalorieGoal = calorieGoal,
                    dailyWaterGoalMl = waterGoal
                )
            )
        }
    }

    fun saveNotificationPref(
        module: String,
        isEnabled: Boolean,
        intervalHours: Int,
        hour: Int,
        minute: Int,
        customMessage: String = ""
    ) {
        viewModelScope.launch {
            val pref = NotificationPrefEntity(
                module = module,
                isEnabled = isEnabled,
                intervalHours = intervalHours.coerceIn(1, 24),
                reminderHour = hour.coerceIn(0, 23),
                reminderMinute = minute.coerceIn(0, 59),
                customMessage = customMessage.trim()
            )
            userRepo.saveNotificationPref(pref)
            if (isEnabled) {
                ReminderScheduler.schedule(context, module, pref)
            } else {
                ReminderScheduler.cancel(context, module)
            }
        }
    }
}
