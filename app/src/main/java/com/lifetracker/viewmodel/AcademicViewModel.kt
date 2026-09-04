package com.lifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifetracker.data.db.AcademicSessionEntity
import com.lifetracker.data.db.SubjectTime
import com.lifetracker.data.repository.AcademicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AcademicState(
    val todaySessions: List<AcademicSessionEntity> = emptyList(),
    val allSessions: List<AcademicSessionEntity> = emptyList(),
    val subjectTimes: List<SubjectTime> = emptyList(),
    val todayTotalMinutes: Int = 0
)

@HiltViewModel
class AcademicViewModel @Inject constructor(
    private val repo: AcademicRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AcademicState())
    val state: StateFlow<AcademicState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getTodaySessions().collect { sessions ->
                _state.update { it.copy(todaySessions = sessions) }
            }
        }
        viewModelScope.launch {
            repo.getAllSessions().collect { sessions ->
                _state.update { it.copy(allSessions = sessions) }
            }
        }
        viewModelScope.launch {
            repo.getTimePerSubject().collect { times ->
                _state.update { it.copy(subjectTimes = times) }
            }
        }
        viewModelScope.launch {
            repo.getTodayTotalMinutes().collect { minutes ->
                _state.update { it.copy(todayTotalMinutes = minutes ?: 0) }
            }
        }
    }

    fun addSession(subject: String, durationMinutes: Int, topic: String = "", notes: String = "") {
        viewModelScope.launch {
            val session = AcademicSessionEntity(
                subject = subject,
                durationMinutes = durationMinutes,
                topic = topic,
                notes = notes,
                date = LocalDate.now().toString()
            )
            repo.addSession(session)
        }
    }

    fun deleteSession(session: AcademicSessionEntity) {
        viewModelScope.launch {
            repo.deleteSession(session)
        }
    }
}
