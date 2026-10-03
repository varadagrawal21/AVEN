package com.lifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifetracker.data.db.QuoteEntity
import com.lifetracker.data.repository.QuotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuotesState(
    val quotes: List<QuoteEntity> = emptyList(),
    val searchQuery: String = "",
    val quoteCount: Int = 0,
    val showFavoritesOnly: Boolean = false
)

@HiltViewModel
class QuotesViewModel @Inject constructor(
    private val repo: QuotesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _state = MutableStateFlow(QuotesState())
    val state: StateFlow<QuotesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_searchQuery.debounce(300), _showFavoritesOnly) { query, favOnly ->
                Pair(query, favOnly)
            }.flatMapLatest { (query, favOnly) ->
                when {
                    favOnly -> repo.getFavoriteQuotes()
                    query.isNotEmpty() -> repo.searchQuotes(query)
                    else -> repo.getAllQuotes()
                }
            }.collect { quotes ->
                _state.update { it.copy(quotes = quotes) }
            }
        }
        viewModelScope.launch {
            repo.getQuoteCount().collect { count ->
                _state.update { it.copy(quoteCount = count) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    fun toggleFavoritesFilter() {
        val newValue = !_state.value.showFavoritesOnly
        _showFavoritesOnly.value = newValue
        _state.update { it.copy(showFavoritesOnly = newValue) }
    }

    fun addQuote(quoteText: String, author: String, source: String, reflection: String, howToApply: String, category: String) {
        viewModelScope.launch {
            val quote = QuoteEntity(
                quoteText = quoteText,
                author = author,
                source = source,
                reflection = reflection,
                howToApply = howToApply,
                category = category,
                dateAdded = ""
            )
            repo.addQuote(quote)
        }
    }

    fun updateQuote(quote: QuoteEntity) {
        viewModelScope.launch {
            repo.updateQuote(quote)
        }
    }

    fun toggleFavorite(quote: QuoteEntity) {
        viewModelScope.launch {
            repo.toggleFavorite(quote)
        }
    }

    fun deleteQuote(quote: QuoteEntity) {
        viewModelScope.launch {
            repo.deleteQuote(quote)
        }
    }

    suspend fun getQuoteById(id: Long): QuoteEntity? = repo.getQuoteById(id)
}
