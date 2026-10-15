package com.cst438.project2.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AdminScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun adminScreenDisplaysUsers() {
        composeTestRule.setContent {
            AdminScreen(
                logout = {}
            )
        }

        composeTestRule
            .onNodeWithText("Admin Page")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Manage users")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Linus Schaub")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("linus@email.com")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Hugo Ruiz-Mireles")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Victor Borba")
            .assertIsDisplayed()
    }

    @Test
    fun adminScreenLogoutButtonCallsLogout() {
        var logoutClicked = false

        composeTestRule.setContent {
            AdminScreen(
                logout = {
                    logoutClicked = true
                }
            )
        }

        composeTestRule
            .onNodeWithText("Logout")
            .performClick()

        assertTrue(logoutClicked)
    }
}
