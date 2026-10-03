package com.lifetracker.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────────────────────
// 1. User / Body Measurements
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val weightKg: Float = 70f,
    val heightCm: Float = 170f,
    val ageYears: Int = 25,
    val dailyWaterGoalMl: Int = 2500,
    val dailyCalorieGoal: Int = 2000,
    val updatedAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// 2. Hydration Logs
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "hydration_logs")
data class HydrationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String  // "YYYY-MM-DD" for easy daily grouping
)

// ─────────────────────────────────────────────────────────────────────────────
// 3. Health Logs (weight, calories, nutrients)
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "health_logs")
data class HealthLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,           // "YYYY-MM-DD"
    val weightKg: Float? = null,
    val caloriesConsumed: Int = 0,
    val proteinG: Float = 0f,
    val carbsG: Float = 0f,
    val fatG: Float = 0f,
    val stepsCount: Int = 0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// 4. Finance Logs
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "finance_logs")
data class FinanceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,           // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String,       // e.g., "Food", "Transport", "Salary"
    val description: String = "",
    val date: String,           // "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// 5. Vocabulary
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val meaning: String,
    val exampleSentence: String = "",
    val partOfSpeech: String = "",  // noun, verb, adjective, etc.
    val pronunciationAudioUrl: String = "",
    val dateAdded: String,          // "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// 6. Academic / Study Sessions
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "academic_sessions")
data class AcademicSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val durationMinutes: Int,
    val topic: String = "",
    val notes: String = "",
    val date: String,           // "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// 7. Books
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String = "",
    val totalPages: Int = 0,
    val pagesRead: Int = 0,
    val status: String = "READING",  // READING, COMPLETED, WANT_TO_READ
    val pdfUri: String = "",         // URI to PDF file on device
    val coverImageUri: String = "",
    val startDate: String = "",
    val finishDate: String = "",
    val rating: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// 8. Book Notes / Learnings
// ─────────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "book_notes",
    foreignKeys = [ForeignKey(
        entity = BookEntity::class,
        parentColumns = ["id"],
        childColumns = ["bookId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("bookId")]
)
data class BookNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val noteType: String = "LEARNING",  // LEARNING, SUMMARY, QUOTE
    val content: String,
    val pageNumber: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// 9. Quotes & Principles
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteText: String,
    val author: String = "",
    val source: String = "",        // book, person, etc.
    val reflection: String = "",    // personal reflection on the quote
    val howToApply: String = "",    // how you apply it in life
    val category: String = "",      // motivation, wisdom, etc.
    val isFavorite: Boolean = false,
    val dateAdded: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// 10. People Room
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relationship: String = "",      // friend, colleague, mentor, etc.
    val characterNotes: String = "",    // personality traits
    val behaviourNotes: String = "",    // how they behave
    val relationshipNotes: String = "", // your relationship dynamics
    val contactInfo: String = "",
    val photoUri: String = "",
    val dateAdded: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// 11. Custom Sections
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "custom_sections")
data class CustomSectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val icon: String = "📝",
    val color: String = "#2196F3",
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// 12. Custom Entries (entries within custom sections)
// ─────────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "custom_entries",
    foreignKeys = [ForeignKey(
        entity = CustomSectionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sectionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sectionId")]
)
data class CustomEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectionId: Long,
    val title: String,
    val content: String = "",
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// 13. Notification Preferences
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "notification_prefs")
data class NotificationPrefEntity(
    @PrimaryKey val module: String,  // "HYDRATION", "STUDY", "FINANCE", etc.
    val isEnabled: Boolean = false,
    val intervalHours: Int = 2,
    val reminderHour: Int = 8,       // hour of day (0-23)
    val reminderMinute: Int = 0,
    val customMessage: String = ""
)
