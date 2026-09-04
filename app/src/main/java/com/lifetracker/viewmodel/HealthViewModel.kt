package com.lifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifetracker.data.db.HealthLogEntity
import com.lifetracker.data.db.UserEntity
import com.lifetracker.data.repository.HealthRepository
import com.lifetracker.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HealthState(
    val todayLog: HealthLogEntity? = null,
    val recentLogs: List<HealthLogEntity> = emptyList(),
    val weightHistory: List<HealthLogEntity> = emptyList(),
    val user: UserEntity? = null,
    val bmi: Float = 0f,
    val bmiCategory: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val healthRepo: HealthRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HealthState())
    val state: StateFlow<HealthState> = _state.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            userRepo.getUser().collect { user ->
                val bmi = if (user != null) healthRepo.calculateBmi(user.weightKg, user.heightCm) else 0f
                _state.update {
                    it.copy(
                        user = user,
                        bmi = bmi,
                        bmiCategory = healthRepo.getBmiCategory(bmi)
                    )
                }
            }
        }
        viewModelScope.launch {
            healthRepo.getTodayLog().collect { log ->
                _state.update { it.copy(todayLog = log) }
            }
        }
        viewModelScope.launch {
            healthRepo.getRecentLogs().collect { logs ->
                _state.update { it.copy(recentLogs = logs) }
            }
        }
        viewModelScope.launch {
            healthRepo.getWeightHistory().collect { history ->
                _state.update { it.copy(weightHistory = history) }
            }
        }
    }

    fun saveHealthLog(
        weightKg: Float?,
        calories: Int,
        proteinG: Float,
        carbsG: Float,
        fatG: Float,
        steps: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val existing = _state.value.todayLog
            val log = (existing ?: HealthLogEntity(date = today)).copy(
                date = today,
                weightKg = weightKg,
                caloriesConsumed = calories,
                proteinG = proteinG,
                carbsG = carbsG,
                fatG = fatG,
                stepsCount = steps,
                notes = notes
            )
            healthRepo.saveLog(log)

            // Update user weight if provided
            if (weightKg != null) {
                val currentUser = _state.value.user ?: UserEntity()
                userRepo.saveUser(currentUser.copy(weightKg = weightKg))
            }
        }
    }

    fun deleteLog(log: HealthLogEntity) {
        viewModelScope.launch {
            healthRepo.deleteLog(log)
        }
    }
}
