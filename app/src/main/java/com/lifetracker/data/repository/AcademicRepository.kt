package com.lifetracker.data.repository

import com.lifetracker.data.db.AcademicSessionDao
import com.lifetracker.data.db.AcademicSessionEntity
import com.lifetracker.data.db.SubjectTime
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AcademicRepository @Inject constructor(
    private val dao: AcademicSessionDao
) {
    fun getAllSessions(): Flow<List<AcademicSessionEntity>> = dao.getAllSessions()

    fun getTodaySessions(): Flow<List<AcademicSessionEntity>> {
        val today = LocalDate.now().toString()
        return dao.getSessionsByDate(today)
    }

    fun getTimePerSubject(): Flow<List<SubjectTime>> = dao.getTimePerSubject()

    fun getTodayTotalMinutes(): Flow<Int?> {
        val today = LocalDate.now().toString()
        return dao.getTotalMinutesForDate(today)
    }

    suspend fun addSession(session: AcademicSessionEntity): Long = dao.insert(session)

    suspend fun updateSession(session: AcademicSessionEntity) = dao.update(session)

    suspend fun deleteSession(session: AcademicSessionEntity) = dao.delete(session)

    suspend fun deleteSessionById(id: Long) = dao.deleteById(id)
}
