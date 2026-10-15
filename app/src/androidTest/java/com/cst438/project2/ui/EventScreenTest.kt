
package com.cst438.project2.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EventScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // checks the Event home screen appears
    @Test
    fun eventScreen_displaysHome() {
        composeRule.setContent {
            EventScreen(
                onHomeClick = {},
                onCreateEventClick = {},
                onProfileClick = {},
                onLogoutClick = {}
            )
        }

        composeRule.onNodeWithText("Event").assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Create Event").assertIsDisplayed()
    }

    // cecks selecting Create Event calls its callback.
    @Test
    fun createEventButton_callsCallback() {
        var createEventClicked = false

        composeRule.setContent {
            EventScreen(
                onHomeClick = {},
                onCreateEventClick = {
                    createEventClicked = true
                },
                onProfileClick = {},
                onLogoutClick = {}
            )
        }

        composeRule
            .onNodeWithText("Create Event")
            .performClick()

        composeRule.waitForIdle()

        assertTrue(createEventClicked)
    }

    // checks the Logout button calls its callback.
    @Test
    fun logoutButton_callsCallback() {
        var logoutClicked = false

        composeRule.setContent {
            EventScreen(
                onHomeClick = {},
                onCreateEventClick = {},
                onProfileClick = {},
                onLogoutClick = {
                    logoutClicked = true
                }
            )
        }

        composeRule
            .onNodeWithText("Log Out")
            .performClick()

        composeRule.waitForIdle()

        assertTrue(logoutClicked)
    }
}
