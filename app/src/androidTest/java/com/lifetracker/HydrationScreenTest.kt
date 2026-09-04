package com.lifetracker

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for critical user flows.
 * These tests run on an Android device or emulator.
 *
 * NOTE: These tests require the Android SDK and an emulator/device.
 * Run with: ./gradlew connectedAndroidTest
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HydrationScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun hydrationScreen_isDisplayed() {
        // Navigate to Hydration tab
        composeTestRule.onNodeWithText("Hydration").performClick()
        composeTestRule.onNodeWithText("💧 Hydration").assertIsDisplayed()
    }

    @Test
    fun addWaterLog_showsInList() {
        // Navigate to Hydration tab
        composeTestRule.onNodeWithText("Hydration").performClick()

        // Click FAB to open add dialog
        composeTestRule.onNodeWithContentDescription("Add Water Log").performClick()

        // Enter amount
        composeTestRule.onNodeWithText("Amount (ml)").performTextInput("250")

        // Click Add
        composeTestRule.onNodeWithText("Add").performClick()

        // Verify the log appears
        composeTestRule.onNodeWithText("250 ml").assertIsDisplayed()
    }

    @Test
    fun quickAddButtons_areDisplayed() {
        composeTestRule.onNodeWithText("Hydration").performClick()
        composeTestRule.onNodeWithText("150ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("250ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("350ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("500ml").assertIsDisplayed()
    }
}
