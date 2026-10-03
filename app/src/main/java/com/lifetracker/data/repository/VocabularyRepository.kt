package com.lifetracker.data.repository

import com.lifetracker.data.db.VocabularyDao
import com.lifetracker.data.db.VocabularyEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRepository @Inject constructor(
    private val dao: VocabularyDao
) {
    fun getAllWords(): Flow<List<VocabularyEntity>> = dao.getAllWords()

    fun searchWords(query: String): Flow<List<VocabularyEntity>> = dao.searchWords(query)

    fun getWordCount(): Flow<Int> = dao.getWordCount()

    suspend fun getWordById(id: Long): VocabularyEntity? = dao.getWordById(id)

    suspend fun addWord(
        word: String,
        meaning: String,
        exampleSentence: String = "",
        partOfSpeech: String = "",
        pronunciationAudioUrl: String = ""
    ): Long {
        val entity = VocabularyEntity(
            word = word.trim(),
            meaning = meaning.trim(),
            exampleSentence = exampleSentence.trim(),
            partOfSpeech = partOfSpeech.trim(),
            pronunciationAudioUrl = pronunciationAudioUrl.trim(),
            dateAdded = LocalDate.now().toString()
        )
        return dao.insert(entity)
    }

    suspend fun updateWord(word: VocabularyEntity) = dao.update(word)

    suspend fun deleteWord(word: VocabularyEntity) = dao.delete(word)

    suspend fun deleteWordById(id: Long) = dao.deleteById(id)
}
