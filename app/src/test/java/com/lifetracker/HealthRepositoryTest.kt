package com.lifetracker

import com.lifetracker.data.repository.HealthRepository
import com.lifetracker.data.db.HealthLogDao
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HealthRepositoryTest {

    private lateinit var dao: HealthLogDao
    private lateinit var repository: HealthRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = HealthRepository(dao)
    }

    @Test
    fun `calculateBmi returns correct value for 70kg 175cm`() {
        val bmi = repository.calculateBmi(70f, 175f)
        // 70 / (1.75 * 1.75) = 70 / 3.0625 ≈ 22.86
        assertEquals(22.86f, bmi, 0.1f)
    }

    @Test
    fun `getBmiCategory returns Normal weight for BMI 22`() {
        val category = repository.getBmiCategory(22f)
        assertEquals("Normal weight", category)
    }

    @Test
    fun `getBmiCategory returns Underweight for BMI 17`() {
        val category = repository.getBmiCategory(17f)
        assertEquals("Underweight", category)
    }

    @Test
    fun `getBmiCategory returns Overweight for BMI 27`() {
        val category = repository.getBmiCategory(27f)
        assertEquals("Overweight", category)
    }

    @Test
    fun `getBmiCategory returns Obese for BMI 35`() {
        val category = repository.getBmiCategory(35f)
        assertEquals("Obese", category)
    }
}
