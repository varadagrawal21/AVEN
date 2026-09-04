package com.lifetracker.data.repository

import com.lifetracker.data.db.HealthLogDao
import com.lifetracker.data.db.HealthLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepository @Inject constructor(
    private val dao: HealthLogDao
) {
    fun getAllLogs(): Flow<List<HealthLogEntity>> = dao.getAllLogs()

    fun getTodayLog(): Flow<HealthLogEntity?> {
        val today = LocalDate.now().toString()
        return dao.getLogByDate(today)
    }

    fun getRecentLogs(): Flow<List<HealthLogEntity>> = dao.getRecentLogs()

    fun getWeightHistory(): Flow<List<HealthLogEntity>> = dao.getWeightHistory()

    suspend fun saveLog(log: HealthLogEntity): Long = dao.insert(log)

    suspend fun updateLog(log: HealthLogEntity) = dao.update(log)

    suspend fun deleteLog(log: HealthLogEntity) = dao.delete(log)

    suspend fun deleteLogById(id: Long) = dao.deleteById(id)

    /**
     * Calculate BMI: weight(kg) / height(m)^2
     */
    fun calculateBmi(weightKg: Float, heightCm: Float): Float {
        val heightM = heightCm / 100f
        return weightKg / (heightM * heightM)
    }

    /**
     * Get BMI category
     */
    fun getBmiCategory(bmi: Float): String = when {
        bmi < 18.5f -> "Underweight"
        bmi < 25.0f -> "Normal weight"
        bmi < 30.0f -> "Overweight"
        else -> "Obese"
    }
}
