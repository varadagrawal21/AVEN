package com.lifetracker.data.repository

import com.lifetracker.data.db.CategoryTotal
import com.lifetracker.data.db.FinanceLogDao
import com.lifetracker.data.db.FinanceLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val dao: FinanceLogDao
) {
    fun getAllLogs(): Flow<List<FinanceLogEntity>> = dao.getAllLogs()

    fun getCurrentMonthLogs(): Flow<List<FinanceLogEntity>> {
        val monthPrefix = LocalDate.now().toString().substring(0, 7) // "YYYY-MM"
        return dao.getLogsByMonth(monthPrefix)
    }

    fun getCurrentMonthIncome(): Flow<Double?> {
        val monthPrefix = LocalDate.now().toString().substring(0, 7)
        return dao.getTotalIncomeForMonth(monthPrefix)
    }

    fun getCurrentMonthExpenses(): Flow<Double?> {
        val monthPrefix = LocalDate.now().toString().substring(0, 7)
        return dao.getTotalExpenseForMonth(monthPrefix)
    }

    fun getCurrentMonthExpenseByCategory(): Flow<List<CategoryTotal>> {
        val monthPrefix = LocalDate.now().toString().substring(0, 7)
        return dao.getExpenseByCategory(monthPrefix)
    }

    suspend fun addLog(log: FinanceLogEntity): Long = dao.insert(log)

    suspend fun updateLog(log: FinanceLogEntity) = dao.update(log)

    suspend fun deleteLog(log: FinanceLogEntity) = dao.delete(log)

    suspend fun deleteLogById(id: Long) = dao.deleteById(id)

    val expenseCategories = listOf(
        "Food & Dining", "Transport", "Shopping", "Entertainment",
        "Health & Medical", "Education", "Utilities", "Rent/Housing",
        "Personal Care", "Travel", "Savings", "Other"
    )

    val incomeCategories = listOf(
        "Salary", "Freelance", "Business", "Investment",
        "Gift", "Bonus", "Other"
    )
}
