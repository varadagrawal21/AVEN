package com.lifetracker.data.repository

import com.lifetracker.data.db.BookDao
import com.lifetracker.data.db.BookEntity
import com.lifetracker.data.db.BookNoteDao
import com.lifetracker.data.db.BookNoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BooksRepository @Inject constructor(
    private val bookDao: BookDao,
    private val bookNoteDao: BookNoteDao
) {
    fun getAllBooks(): Flow<List<BookEntity>> = bookDao.getAllBooks()

    fun getBooksByStatus(status: String): Flow<List<BookEntity>> = bookDao.getBooksByStatus(status)

    fun getCompletedBookCount(): Flow<Int> = bookDao.getCompletedBookCount()

    suspend fun getBookById(id: Long): BookEntity? = bookDao.getBookById(id)

    suspend fun addBook(book: BookEntity): Long = bookDao.insert(book)

    suspend fun updateBook(book: BookEntity) = bookDao.update(book)

    suspend fun deleteBook(book: BookEntity) = bookDao.delete(book)

    suspend fun deleteBookById(id: Long) = bookDao.deleteById(id)

    // Book Notes
    fun getNotesForBook(bookId: Long): Flow<List<BookNoteEntity>> = bookNoteDao.getNotesForBook(bookId)

    suspend fun addNote(note: BookNoteEntity): Long = bookNoteDao.insert(note)

    suspend fun updateNote(note: BookNoteEntity) = bookNoteDao.update(note)

    suspend fun deleteNote(note: BookNoteEntity) = bookNoteDao.delete(note)

    suspend fun deleteNoteById(id: Long) = bookNoteDao.deleteById(id)

    fun getReadingProgress(book: BookEntity): Float {
        return if (book.totalPages > 0) {
            book.pagesRead.toFloat() / book.totalPages.toFloat()
        } else 0f
    }
}
