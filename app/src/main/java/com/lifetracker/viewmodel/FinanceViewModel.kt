package com.lifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifetracker.data.db.CategoryTotal
import com.lifetracker.data.db.FinanceLogEntity
import com.lifetracker.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FinanceState(
    val logs: List<FinanceLogEntity> = emptyList(),
    val monthIncome: Double = 0.0,
    val monthExpenses: Double = 0.0,
    val categoryBreakdown: List<CategoryTotal> = emptyList(),
    val expenseCategories: List<String> = emptyList(),
    val incomeCategories: List<String> = emptyList()
) {
    val balance: Double get() = monthIncome - monthExpenses
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeRepo: FinanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FinanceState(
        expenseCategories = financeRepo.expenseCategories,
        incomeCategories = financeRepo.incomeCategories
    ))
    val state: StateFlow<FinanceState> = _state.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            financeRepo.getCurrentMonthLogs().collect { logs ->
                _state.update { it.copy(logs = logs) }
            }
        }
        viewModelScope.launch {
            financeRepo.getCurrentMonthIncome().collect { income ->
                _state.update { it.copy(monthIncome = income ?: 0.0) }
            }
        }
        viewModelScope.launch {
            financeRepo.getCurrentMonthExpenses().collect { expenses ->
                _state.update { it.copy(monthExpenses = expenses ?: 0.0) }
            }
        }
        viewModelScope.launch {
            financeRepo.getCurrentMonthExpenseByCategory().collect { breakdown ->
                _state.update { it.copy(categoryBreakdown = breakdown) }
            }
        }
    }

    fun addLog(type: String, amount: Double, category: String, description: String) {
        viewModelScope.launch {
            val log = FinanceLogEntity(
                type = type,
                amount = amount,
                category = category,
                description = description,
                date = LocalDate.now().toString()
            )
            financeRepo.addLog(log)
        }
    }

    fun deleteLog(log: FinanceLogEntity) {
        viewModelScope.launch {
            financeRepo.deleteLog(log)
        }
    }
}
