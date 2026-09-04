package com.lifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifetracker.data.db.CustomEntryEntity
import com.lifetracker.data.db.CustomSectionEntity
import com.lifetracker.data.repository.CustomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomState(
    val sections: List<CustomSectionEntity> = emptyList(),
    val selectedSectionEntries: List<CustomEntryEntity> = emptyList()
)

@HiltViewModel
class CustomViewModel @Inject constructor(
    private val repo: CustomRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CustomState())
    val state: StateFlow<CustomState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAllSections().collect { sections ->
                _state.update { it.copy(sections = sections) }
            }
        }
    }

    fun loadEntriesForSection(sectionId: Long) {
        viewModelScope.launch {
            repo.getEntriesForSection(sectionId).collect { entries ->
                _state.update { it.copy(selectedSectionEntries = entries) }
            }
        }
    }

    fun addSection(name: String, description: String, icon: String, color: String) {
        viewModelScope.launch {
            val section = CustomSectionEntity(name = name, description = description, icon = icon, color = color)
            repo.addSection(section)
        }
    }

    fun deleteSection(section: CustomSectionEntity) {
        viewModelScope.launch {
            repo.deleteSection(section)
        }
    }

    fun addEntry(sectionId: Long, title: String, content: String) {
        viewModelScope.launch {
            val entry = CustomEntryEntity(sectionId = sectionId, title = title, content = content, date = "")
            repo.addEntry(entry)
        }
    }

    fun deleteEntry(entry: CustomEntryEntity) {
        viewModelScope.launch {
            repo.deleteEntry(entry)
        }
    }

    suspend fun getSectionById(id: Long): CustomSectionEntity? = repo.getSectionById(id)
}
