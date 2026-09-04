package com.lifetracker.data.repository

import com.lifetracker.data.db.QuoteDao
import com.lifetracker.data.db.QuoteEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuotesRepository @Inject constructor(
    private val dao: QuoteDao
) {
    fun getAllQuotes(): Flow<List<QuoteEntity>> = dao.getAllQuotes()

    fun getFavoriteQuotes(): Flow<List<QuoteEntity>> = dao.getFavoriteQuotes()

    fun searchQuotes(query: String): Flow<List<QuoteEntity>> = dao.searchQuotes(query)

    fun getQuoteCount(): Flow<Int> = dao.getQuoteCount()

    suspend fun getQuoteById(id: Long): QuoteEntity? = dao.getQuoteById(id)

    suspend fun addQuote(quote: QuoteEntity): Long {
        val withDate = if (quote.dateAdded.isEmpty()) {
            quote.copy(dateAdded = LocalDate.now().toString())
        } else quote
        return dao.insert(withDate)
    }

    suspend fun updateQuote(quote: QuoteEntity) = dao.update(quote)

    suspend fun toggleFavorite(quote: QuoteEntity) = dao.update(quote.copy(isFavorite = !quote.isFavorite))

    suspend fun deleteQuote(quote: QuoteEntity) = dao.delete(quote)

    suspend fun deleteQuoteById(id: Long) = dao.deleteById(id)
}
