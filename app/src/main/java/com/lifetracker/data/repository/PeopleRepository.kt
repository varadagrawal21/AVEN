package com.lifetracker.data.repository

import com.lifetracker.data.db.PersonDao
import com.lifetracker.data.db.PersonEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleRepository @Inject constructor(
    private val dao: PersonDao
) {
    fun getAllPeople(): Flow<List<PersonEntity>> = dao.getAllPeople()

    fun searchPeople(query: String): Flow<List<PersonEntity>> = dao.searchPeople(query)

    suspend fun getPersonById(id: Long): PersonEntity? = dao.getPersonById(id)

    suspend fun addPerson(person: PersonEntity): Long {
        val withDate = if (person.dateAdded.isEmpty()) {
            person.copy(dateAdded = LocalDate.now().toString())
        } else person
        return dao.insert(withDate)
    }

    suspend fun updatePerson(person: PersonEntity) = dao.update(person)

    suspend fun deletePerson(person: PersonEntity) = dao.delete(person)

    suspend fun deletePersonById(id: Long) = dao.deleteById(id)
}
