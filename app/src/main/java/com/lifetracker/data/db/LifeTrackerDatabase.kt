package com.lifetracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        HydrationLogEntity::class,
        HealthLogEntity::class,
        FinanceLogEntity::class,
        VocabularyEntity::class,
        AcademicSessionEntity::class,
        BookEntity::class,
        BookNoteEntity::class,
        QuoteEntity::class,
        PersonEntity::class,
        CustomSectionEntity::class,
        CustomEntryEntity::class,
        NotificationPrefEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class LifeTrackerDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun hydrationLogDao(): HydrationLogDao
    abstract fun healthLogDao(): HealthLogDao
    abstract fun financeLogDao(): FinanceLogDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun academicSessionDao(): AcademicSessionDao
    abstract fun bookDao(): BookDao
    abstract fun bookNoteDao(): BookNoteDao
    abstract fun quoteDao(): QuoteDao
    abstract fun personDao(): PersonDao
    abstract fun customSectionDao(): CustomSectionDao
    abstract fun customEntryDao(): CustomEntryDao
    abstract fun notificationPrefDao(): NotificationPrefDao

    companion object {
        const val DATABASE_NAME = "lifetracker_db"
    }
}
