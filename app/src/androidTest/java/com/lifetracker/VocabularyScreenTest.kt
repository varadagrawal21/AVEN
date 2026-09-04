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
class VocabularyScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun vocabularyScreen_isDisplayed() {
        composeTestRule.onNodeWithText("Vocab").performClick()
        composeTestRule.onNodeWithText("Search words...").assertIsDisplayed()
    }

    @Test
    fun addWord_showsInList() {
        composeTestRule.onNodeWithText("Vocab").performClick()

        // Click FAB
        composeTestRule.onNodeWithContentDescription("Add Word").performClick()

        // Enter word and meaning
        composeTestRule.onNodeWithText("Word *").performTextInput("serendipity")
        composeTestRule.onNodeWithText("Meaning *").performTextInput("finding something good without looking for it")

        // Click Add
        composeTestRule.onNodeWithText("Add").performClick()

        // Verify word appears
        composeTestRule.onNodeWithText("serendipity").assertIsDisplayed()
    }
}
