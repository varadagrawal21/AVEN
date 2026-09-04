package com.lifetracker

import com.lifetracker.data.db.VocabularyDao
import com.lifetracker.data.db.VocabularyEntity
import com.lifetracker.data.repository.VocabularyRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class VocabularyRepositoryTest {

    private lateinit var dao: VocabularyDao
    private lateinit var repository: VocabularyRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = VocabularyRepository(dao)
    }

    @Test
    fun `addWord calls dao insert with correct entity`() = runTest {
        coEvery { dao.insert(any()) } returns 1L
        repository.addWord("ephemeral", "lasting for a very short time", "The ephemeral beauty of cherry blossoms.", "adjective")
        coVerify {
            dao.insert(match { entity ->
                entity.word == "ephemeral" &&
                entity.meaning == "lasting for a very short time" &&
                entity.exampleSentence == "The ephemeral beauty of cherry blossoms." &&
                entity.partOfSpeech == "adjective"
            })
        }
    }

    @Test
    fun `deleteWord calls dao delete`() = runTest {
        val word = VocabularyEntity(id = 1, word = "test", meaning = "a test", dateAdded = "2024-01-01")
        repository.deleteWord(word)
        coVerify { dao.delete(word) }
    }

    @Test
    fun `addWord trims whitespace`() = runTest {
        coEvery { dao.insert(any()) } returns 1L
        repository.addWord("  hello  ", "  a greeting  ")
        coVerify {
            dao.insert(match { entity ->
                entity.word == "hello" && entity.meaning == "a greeting"
            })
        }
    }
}
