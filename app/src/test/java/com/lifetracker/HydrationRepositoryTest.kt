package com.lifetracker

import com.lifetracker.data.db.HydrationLogDao
import com.lifetracker.data.db.HydrationLogEntity
import com.lifetracker.data.repository.HydrationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HydrationRepositoryTest {

    private lateinit var dao: HydrationLogDao
    private lateinit var repository: HydrationRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = HydrationRepository(dao)
    }

    @Test
    fun `calculateDailyGoal returns correct value`() {
        // 70kg * 35ml = 2450ml
        val goal = repository.calculateDailyGoal(70f)
        assertEquals(2450, goal)
    }

    @Test
    fun `calculateDailyGoal for 80kg returns 2800ml`() {
        val goal = repository.calculateDailyGoal(80f)
        assertEquals(2800, goal)
    }

    @Test
    fun `addLog calls dao insert`() = runTest {
        coEvery { dao.insert(any()) } returns 1L
        repository.addLog(250, "Morning water")
        coVerify { dao.insert(any()) }
    }

    @Test
    fun `deleteLog calls dao delete`() = runTest {
        val log = HydrationLogEntity(id = 1, amountMl = 250, date = "2024-01-01")
        repository.deleteLog(log)
        coVerify { dao.delete(log) }
    }

    @Test
    fun `getTotalForToday returns flow from dao`() = runTest {
        every { dao.getTotalForDate(any()) } returns flowOf(500)
        val flow = repository.getTotalForToday()
        flow.collect { total ->
            assertEquals(500, total)
        }
    }
}
