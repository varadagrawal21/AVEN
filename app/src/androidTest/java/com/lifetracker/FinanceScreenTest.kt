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

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FinanceScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun financeScreen_isDisplayed() {
        composeTestRule.onNodeWithText("Finance").performClick()
        composeTestRule.onNodeWithText("💰 Finance & Expenses").assertIsDisplayed()
    }

    @Test
    fun financeScreen_showsMonthlyBalance() {
        composeTestRule.onNodeWithText("Finance").performClick()
        composeTestRule.onNodeWithText("This Month").assertIsDisplayed()
        composeTestRule.onNodeWithText("Income").assertIsDisplayed()
        composeTestRule.onNodeWithText("Expenses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Balance").assertIsDisplayed()
    }

    @Test
    fun addExpense_opensDialog() {
        composeTestRule.onNodeWithText("Finance").performClick()
        composeTestRule.onNodeWithContentDescription("Add Entry").performClick()
        composeTestRule.onNodeWithText("Add Transaction").assertIsDisplayed()
    }
}
