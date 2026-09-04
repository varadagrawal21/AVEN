package com.lifetracker.di

import android.content.Context
import androidx.room.Room
import com.lifetracker.data.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LifeTrackerDatabase {
        return Room.databaseBuilder(
            context,
            LifeTrackerDatabase::class.java,
            LifeTrackerDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: LifeTrackerDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideHydrationLogDao(db: LifeTrackerDatabase): HydrationLogDao = db.hydrationLogDao()

    @Provides
    @Singleton
    fun provideHealthLogDao(db: LifeTrackerDatabase): HealthLogDao = db.healthLogDao()

    @Provides
    @Singleton
    fun provideFinanceLogDao(db: LifeTrackerDatabase): FinanceLogDao = db.financeLogDao()

    @Provides
    @Singleton
    fun provideVocabularyDao(db: LifeTrackerDatabase): VocabularyDao = db.vocabularyDao()

    @Provides
    @Singleton
    fun provideAcademicSessionDao(db: LifeTrackerDatabase): AcademicSessionDao = db.academicSessionDao()

    @Provides
    @Singleton
    fun provideBookDao(db: LifeTrackerDatabase): BookDao = db.bookDao()

    @Provides
    @Singleton
    fun provideBookNoteDao(db: LifeTrackerDatabase): BookNoteDao = db.bookNoteDao()

    @Provides
    @Singleton
    fun provideQuoteDao(db: LifeTrackerDatabase): QuoteDao = db.quoteDao()

    @Provides
    @Singleton
    fun providePersonDao(db: LifeTrackerDatabase): PersonDao = db.personDao()

    @Provides
    @Singleton
    fun provideCustomSectionDao(db: LifeTrackerDatabase): CustomSectionDao = db.customSectionDao()

    @Provides
    @Singleton
    fun provideCustomEntryDao(db: LifeTrackerDatabase): CustomEntryDao = db.customEntryDao()

    @Provides
    @Singleton
    fun provideNotificationPrefDao(db: LifeTrackerDatabase): NotificationPrefDao = db.notificationPrefDao()
}
