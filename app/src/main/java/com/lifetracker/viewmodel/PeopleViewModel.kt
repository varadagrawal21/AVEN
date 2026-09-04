package com.lifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifetracker.data.db.PersonEntity
import com.lifetracker.data.repository.PeopleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PeopleState(
    val people: List<PersonEntity> = emptyList(),
    val searchQuery: String = ""
)

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val repo: PeopleRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _state = MutableStateFlow(PeopleState())
    val state: StateFlow<PeopleState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery.debounce(300).flatMapLatest { query ->
                if (query.isEmpty()) repo.getAllPeople() else repo.searchPeople(query)
            }.collect { people ->
                _state.update { it.copy(people = people) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    fun addPerson(name: String, relationship: String, characterNotes: String, behaviourNotes: String, relationshipNotes: String, contactInfo: String) {
        viewModelScope.launch {
            val person = PersonEntity(
                name = name,
                relationship = relationship,
                characterNotes = characterNotes,
                behaviourNotes = behaviourNotes,
                relationshipNotes = relationshipNotes,
                contactInfo = contactInfo,
                dateAdded = ""
            )
            repo.addPerson(person)
        }
    }

    fun updatePerson(person: PersonEntity) {
        viewModelScope.launch {
            repo.updatePerson(person)
        }
    }

    fun deletePerson(person: PersonEntity) {
        viewModelScope.launch {
            repo.deletePerson(person)
        }
    }

    suspend fun getPersonById(id: Long): PersonEntity? = repo.getPersonById(id)
}
