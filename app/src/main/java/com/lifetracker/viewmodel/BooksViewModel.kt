package com.lifetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifetracker.data.db.BookEntity
import com.lifetracker.data.db.BookNoteEntity
import com.lifetracker.data.repository.BooksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BooksState(
    val allBooks: List<BookEntity> = emptyList(),
    val readingBooks: List<BookEntity> = emptyList(),
    val completedBooks: List<BookEntity> = emptyList(),
    val completedCount: Int = 0,
    val selectedBook: BookEntity? = null,
    val bookNotes: List<BookNoteEntity> = emptyList()
)

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val repo: BooksRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BooksState())
    val state: StateFlow<BooksState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAllBooks().collect { books ->
                _state.update {
                    it.copy(
                        allBooks = books,
                        readingBooks = books.filter { b -> b.status == "READING" },
                        completedBooks = books.filter { b -> b.status == "COMPLETED" }
                    )
                }
            }
        }
        viewModelScope.launch {
            repo.getCompletedBookCount().collect { count ->
                _state.update { it.copy(completedCount = count) }
            }
        }
    }

    fun loadBookNotes(bookId: Long) {
        viewModelScope.launch {
            repo.getNotesForBook(bookId).collect { notes ->
                _state.update { it.copy(bookNotes = notes) }
            }
        }
    }

    fun addBook(title: String, author: String, totalPages: Int, pdfUri: String = "") {
        viewModelScope.launch {
            val book = BookEntity(
                title = title,
                author = author,
                totalPages = totalPages,
                pdfUri = pdfUri,
                status = "READING"
            )
            repo.addBook(book)
        }
    }

    fun updatePagesRead(book: BookEntity, pagesRead: Int) {
        viewModelScope.launch {
            val status = if (pagesRead >= book.totalPages && book.totalPages > 0) "COMPLETED" else "READING"
            repo.updateBook(book.copy(pagesRead = pagesRead, status = status))
        }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            repo.deleteBook(book)
        }
    }

    fun addNote(bookId: Long, content: String, noteType: String = "LEARNING", pageNumber: Int = 0) {
        viewModelScope.launch {
            val note = BookNoteEntity(
                bookId = bookId,
                content = content,
                noteType = noteType,
                pageNumber = pageNumber
            )
            repo.addNote(note)
        }
    }

    fun deleteNote(note: BookNoteEntity) {
        viewModelScope.launch {
            repo.deleteNote(note)
        }
    }

    suspend fun getBookById(id: Long): BookEntity? = repo.getBookById(id)

    fun getReadingProgress(book: BookEntity): Float = repo.getReadingProgress(book)
}
