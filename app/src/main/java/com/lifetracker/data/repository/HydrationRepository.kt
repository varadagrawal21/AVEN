package com.lifetracker.data.repository

import com.lifetracker.data.db.HydrationLogDao
import com.lifetracker.data.db.HydrationLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HydrationRepository @Inject constructor(
    private val dao: HydrationLogDao
) {
    fun getAllLogs(): Flow<List<HydrationLogEntity>> = dao.getAllLogs()

    fun getLogsForToday(): Flow<List<HydrationLogEntity>> {
        val today = LocalDate.now().toString()
        return dao.getLogsByDate(today)
    }

    fun getTotalForToday(): Flow<Int?> {
        val today = LocalDate.now().toString()
        return dao.getTotalForDate(today)
    }

    fun getTotalForDate(date: String): Flow<Int?> = dao.getTotalForDate(date)

    fun getRecentLogs(): Flow<List<HydrationLogEntity>> = dao.getRecentLogs()

    suspend fun addLog(amountMl: Int, note: String = ""): Long {
        val today = LocalDate.now().toString()
        val log = HydrationLogEntity(
            amountMl = amountMl,
            note = note,
            date = today
        )
        return dao.insert(log)
    }

    suspend fun deleteLog(log: HydrationLogEntity) = dao.delete(log)

    suspend fun deleteLogById(id: Long) = dao.deleteById(id)

    /**
     * Calculate recommended daily water intake based on body weight.
     * General formula: 35ml per kg of body weight
     */
    fun calculateDailyGoal(weightKg: Float): Int {
        return (weightKg * 35).toInt()
    }
}
