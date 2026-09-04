package com.lifetracker.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────────────
// UserDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)
}

// ─────────────────────────────────────────────────────────────────────────────
// HydrationLogDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface HydrationLogDao {
    @Query("SELECT * FROM hydration_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<HydrationLogEntity>>

    @Query("SELECT * FROM hydration_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getLogsByDate(date: String): Flow<List<HydrationLogEntity>>

    @Query("SELECT SUM(amountMl) FROM hydration_logs WHERE date = :date")
    fun getTotalForDate(date: String): Flow<Int?>

    @Query("SELECT * FROM hydration_logs ORDER BY timestamp DESC LIMIT 30")
    fun getRecentLogs(): Flow<List<HydrationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: HydrationLogEntity): Long

    @Update
    suspend fun update(log: HydrationLogEntity)

    @Delete
    suspend fun delete(log: HydrationLogEntity)

    @Query("DELETE FROM hydration_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// HealthLogDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface HealthLogDao {
    @Query("SELECT * FROM health_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<HealthLogEntity>>

    @Query("SELECT * FROM health_logs WHERE date = :date LIMIT 1")
    fun getLogByDate(date: String): Flow<HealthLogEntity?>

    @Query("SELECT * FROM health_logs ORDER BY date DESC LIMIT 30")
    fun getRecentLogs(): Flow<List<HealthLogEntity>>

    @Query("SELECT * FROM health_logs WHERE weightKg IS NOT NULL ORDER BY date DESC LIMIT 30")
    fun getWeightHistory(): Flow<List<HealthLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: HealthLogEntity): Long

    @Update
    suspend fun update(log: HealthLogEntity)

    @Delete
    suspend fun delete(log: HealthLogEntity)

    @Query("DELETE FROM health_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// FinanceLogDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface FinanceLogDao {
    @Query("SELECT * FROM finance_logs ORDER BY date DESC, timestamp DESC")
    fun getAllLogs(): Flow<List<FinanceLogEntity>>

    @Query("SELECT * FROM finance_logs WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getLogsByMonth(monthPrefix: String): Flow<List<FinanceLogEntity>>

    @Query("SELECT SUM(amount) FROM finance_logs WHERE type = 'INCOME' AND date LIKE :monthPrefix || '%'")
    fun getTotalIncomeForMonth(monthPrefix: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM finance_logs WHERE type = 'EXPENSE' AND date LIKE :monthPrefix || '%'")
    fun getTotalExpenseForMonth(monthPrefix: String): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM finance_logs WHERE type = 'EXPENSE' AND date LIKE :monthPrefix || '%' GROUP BY category")
    fun getExpenseByCategory(monthPrefix: String): Flow<List<CategoryTotal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: FinanceLogEntity): Long

    @Update
    suspend fun update(log: FinanceLogEntity)

    @Delete
    suspend fun delete(log: FinanceLogEntity)

    @Query("DELETE FROM finance_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class CategoryTotal(val category: String, val total: Double)

// ─────────────────────────────────────────────────────────────────────────────
// VocabularyDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary ORDER BY timestamp DESC")
    fun getAllWords(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE word LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchWords(query: String): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    suspend fun getWordById(id: Long): VocabularyEntity?

    @Query("SELECT COUNT(*) FROM vocabulary")
    fun getWordCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: VocabularyEntity): Long

    @Update
    suspend fun update(word: VocabularyEntity)

    @Delete
    suspend fun delete(word: VocabularyEntity)

    @Query("DELETE FROM vocabulary WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// AcademicSessionDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface AcademicSessionDao {
    @Query("SELECT * FROM academic_sessions ORDER BY date DESC, timestamp DESC")
    fun getAllSessions(): Flow<List<AcademicSessionEntity>>

    @Query("SELECT * FROM academic_sessions WHERE date = :date ORDER BY timestamp DESC")
    fun getSessionsByDate(date: String): Flow<List<AcademicSessionEntity>>

    @Query("SELECT subject, SUM(durationMinutes) as totalMinutes FROM academic_sessions GROUP BY subject ORDER BY totalMinutes DESC")
    fun getTimePerSubject(): Flow<List<SubjectTime>>

    @Query("SELECT SUM(durationMinutes) FROM academic_sessions WHERE date = :date")
    fun getTotalMinutesForDate(date: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: AcademicSessionEntity): Long

    @Update
    suspend fun update(session: AcademicSessionEntity)

    @Delete
    suspend fun delete(session: AcademicSessionEntity)

    @Query("DELETE FROM academic_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class SubjectTime(val subject: String, val totalMinutes: Int)

// ─────────────────────────────────────────────────────────────────────────────
// BookDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY timestamp DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE status = :status ORDER BY timestamp DESC")
    fun getBooksByStatus(status: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): BookEntity?

    @Query("SELECT COUNT(*) FROM books WHERE status = 'COMPLETED'")
    fun getCompletedBookCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Delete
    suspend fun delete(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// BookNoteDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface BookNoteDao {
    @Query("SELECT * FROM book_notes WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getNotesForBook(bookId: Long): Flow<List<BookNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: BookNoteEntity): Long

    @Update
    suspend fun update(note: BookNoteEntity)

    @Delete
    suspend fun delete(note: BookNoteEntity)

    @Query("DELETE FROM book_notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// QuoteDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY timestamp DESC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE quoteText LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchQuotes(query: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Long): QuoteEntity?

    @Query("SELECT COUNT(*) FROM quotes")
    fun getQuoteCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quote: QuoteEntity): Long

    @Update
    suspend fun update(quote: QuoteEntity)

    @Delete
    suspend fun delete(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// PersonDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY name ASC")
    fun getAllPeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE name LIKE '%' || :query || '%' OR relationship LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchPeople(query: String): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getPersonById(id: Long): PersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: PersonEntity): Long

    @Update
    suspend fun update(person: PersonEntity)

    @Delete
    suspend fun delete(person: PersonEntity)

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// CustomSectionDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface CustomSectionDao {
    @Query("SELECT * FROM custom_sections ORDER BY timestamp DESC")
    fun getAllSections(): Flow<List<CustomSectionEntity>>

    @Query("SELECT * FROM custom_sections WHERE id = :id")
    suspend fun getSectionById(id: Long): CustomSectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(section: CustomSectionEntity): Long

    @Update
    suspend fun update(section: CustomSectionEntity)

    @Delete
    suspend fun delete(section: CustomSectionEntity)

    @Query("DELETE FROM custom_sections WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// CustomEntryDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface CustomEntryDao {
    @Query("SELECT * FROM custom_entries WHERE sectionId = :sectionId ORDER BY timestamp DESC")
    fun getEntriesForSection(sectionId: Long): Flow<List<CustomEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CustomEntryEntity): Long

    @Update
    suspend fun update(entry: CustomEntryEntity)

    @Delete
    suspend fun delete(entry: CustomEntryEntity)

    @Query("DELETE FROM custom_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// NotificationPrefDao
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface NotificationPrefDao {
    @Query("SELECT * FROM notification_prefs")
    fun getAllPrefs(): Flow<List<NotificationPrefEntity>>

    @Query("SELECT * FROM notification_prefs WHERE module = :module")
    fun getPrefForModule(module: String): Flow<NotificationPrefEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(pref: NotificationPrefEntity)

    @Delete
    suspend fun delete(pref: NotificationPrefEntity)
}
