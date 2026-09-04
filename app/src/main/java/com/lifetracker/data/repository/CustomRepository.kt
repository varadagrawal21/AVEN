package com.lifetracker.data.repository

import com.lifetracker.data.db.CustomEntryDao
import com.lifetracker.data.db.CustomEntryEntity
import com.lifetracker.data.db.CustomSectionDao
import com.lifetracker.data.db.CustomSectionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomRepository @Inject constructor(
    private val sectionDao: CustomSectionDao,
    private val entryDao: CustomEntryDao
) {
    // Sections
    fun getAllSections(): Flow<List<CustomSectionEntity>> = sectionDao.getAllSections()

    suspend fun getSectionById(id: Long): CustomSectionEntity? = sectionDao.getSectionById(id)

    suspend fun addSection(section: CustomSectionEntity): Long = sectionDao.insert(section)

    suspend fun updateSection(section: CustomSectionEntity) = sectionDao.update(section)

    suspend fun deleteSection(section: CustomSectionEntity) = sectionDao.delete(section)

    suspend fun deleteSectionById(id: Long) = sectionDao.deleteById(id)

    // Entries
    fun getEntriesForSection(sectionId: Long): Flow<List<CustomEntryEntity>> =
        entryDao.getEntriesForSection(sectionId)

    suspend fun addEntry(entry: CustomEntryEntity): Long {
        val withDate = if (entry.date.isEmpty()) {
            entry.copy(date = LocalDate.now().toString())
        } else entry
        return entryDao.insert(withDate)
    }

    suspend fun updateEntry(entry: CustomEntryEntity) = entryDao.update(entry)

    suspend fun deleteEntry(entry: CustomEntryEntity) = entryDao.delete(entry)

    suspend fun deleteEntryById(id: Long) = entryDao.deleteById(id)
}
