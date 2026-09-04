package com.lifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifetracker.data.db.VocabularyEntity
import com.lifetracker.data.repository.VocabularyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VocabularyState(
    val words: List<VocabularyEntity> = emptyList(),
    val searchQuery: String = "",
    val wordCount: Int = 0,
    val selectedWord: VocabularyEntity? = null
)

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val repo: VocabularyRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _state = MutableStateFlow(VocabularyState())
    val state: StateFlow<VocabularyState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .flatMapLatest { query ->
                    if (query.isEmpty()) repo.getAllWords() else repo.searchWords(query)
                }
                .collect { words ->
                    _state.update { it.copy(words = words) }
                }
        }
        viewModelScope.launch {
            repo.getWordCount().collect { count ->
                _state.update { it.copy(wordCount = count) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    fun addWord(word: String, meaning: String, example: String = "", partOfSpeech: String = "") {
        viewModelScope.launch {
            repo.addWord(word, meaning, example, partOfSpeech)
        }
    }

    fun updateWord(word: VocabularyEntity) {
        viewModelScope.launch {
            repo.updateWord(word)
        }
    }

    fun deleteWord(word: VocabularyEntity) {
        viewModelScope.launch {
            repo.deleteWord(word)
        }
    }

    fun selectWord(word: VocabularyEntity?) {
        _state.update { it.copy(selectedWord = word) }
    }

    suspend fun getWordById(id: Long): VocabularyEntity? = repo.getWordById(id)
}
