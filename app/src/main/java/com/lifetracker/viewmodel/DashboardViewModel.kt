package com.lifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifetracker.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val todayWaterMl: Int = 0,
    val waterGoalMl: Int = 2500,
    val todayCalories: Int = 0,
    val calorieGoal: Int = 2000,
    val monthBalance: Double = 0.0,
    val wordCount: Int = 0,
    val todayStudyMinutes: Int = 0,
    val completedBooks: Int = 0,
    val quoteCount: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val hydrationRepo: HydrationRepository,
    private val healthRepo: HealthRepository,
    private val financeRepo: FinanceRepository,
    private val vocabularyRepo: VocabularyRepository,
    private val academicRepo: AcademicRepository,
    private val booksRepo: BooksRepository,
    private val quotesRepo: QuotesRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Combine all flows
            combine(
                hydrationRepo.getTotalForToday(),
                userRepo.getUser(),
                financeRepo.getCurrentMonthIncome(),
                financeRepo.getCurrentMonthExpenses(),
                vocabularyRepo.getWordCount(),
                academicRepo.getTodayTotalMinutes(),
                booksRepo.getCompletedBookCount(),
                quotesRepo.getQuoteCount()
            ) { values ->
                val waterMl = (values[0] as? Int) ?: 0
                val user = values[1]
                val income = (values[2] as? Double) ?: 0.0
                val expenses = (values[3] as? Double) ?: 0.0
                val wordCount = (values[4] as? Int) ?: 0
                val studyMinutes = (values[5] as? Int) ?: 0
                val completedBooks = (values[6] as? Int) ?: 0
                val quoteCount = (values[7] as? Int) ?: 0

                DashboardState(
                    todayWaterMl = waterMl,
                    waterGoalMl = (user as? com.lifetracker.data.db.UserEntity)?.dailyWaterGoalMl ?: 2500,
                    calorieGoal = (user as? com.lifetracker.data.db.UserEntity)?.dailyCalorieGoal ?: 2000,
                    monthBalance = income - expenses,
                    wordCount = wordCount,
                    todayStudyMinutes = studyMinutes,
                    completedBooks = completedBooks,
                    quoteCount = quoteCount
                )
            }.collect { newState ->
                _state.value = newState
            }
        }

        // Load today's health log separately
        viewModelScope.launch {
            healthRepo.getTodayLog().collect { log ->
                _state.update { it.copy(todayCalories = log?.caloriesConsumed ?: 0) }
            }
        }
    }
}
