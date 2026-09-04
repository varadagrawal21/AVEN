package com.lifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifetracker.data.db.HydrationLogEntity
import com.lifetracker.data.db.UserEntity
import com.lifetracker.data.repository.HydrationRepository
import com.lifetracker.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HydrationState(
    val todayLogs: List<HydrationLogEntity> = emptyList(),
    val todayTotalMl: Int = 0,
    val dailyGoalMl: Int = 2500,
    val user: UserEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() = if (dailyGoalMl > 0) (todayTotalMl.toFloat() / dailyGoalMl).coerceIn(0f, 1f) else 0f
}

@HiltViewModel
class HydrationViewModel @Inject constructor(
    private val hydrationRepo: HydrationRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HydrationState())
    val state: StateFlow<HydrationState> = _state.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            userRepo.getUser().collect { user ->
                val goal = user?.dailyWaterGoalMl ?: 2500
                _state.update { it.copy(user = user, dailyGoalMl = goal) }
            }
        }
        viewModelScope.launch {
            hydrationRepo.getLogsForToday().collect { logs ->
                _state.update { it.copy(todayLogs = logs) }
            }
        }
        viewModelScope.launch {
            hydrationRepo.getTotalForToday().collect { total ->
                _state.update { it.copy(todayTotalMl = total ?: 0) }
            }
        }
    }

    fun addWaterLog(amountMl: Int, note: String = "") {
        viewModelScope.launch {
            hydrationRepo.addLog(amountMl, note)
        }
    }

    fun deleteLog(log: HydrationLogEntity) {
        viewModelScope.launch {
            hydrationRepo.deleteLog(log)
        }
    }

    fun updateGoalFromWeight(weightKg: Float) {
        val newGoal = hydrationRepo.calculateDailyGoal(weightKg)
        viewModelScope.launch {
            val currentUser = _state.value.user ?: UserEntity()
            userRepo.saveUser(currentUser.copy(
                weightKg = weightKg,
                dailyWaterGoalMl = newGoal
            ))
        }
    }

    fun setCustomGoal(goalMl: Int) {
        viewModelScope.launch {
            val currentUser = _state.value.user ?: UserEntity()
            userRepo.saveUser(currentUser.copy(dailyWaterGoalMl = goalMl))
        }
    }
}
