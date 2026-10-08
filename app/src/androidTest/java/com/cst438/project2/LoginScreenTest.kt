package com.cst438.project2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cst438.project2.ui.LoginScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun successfulLogin() {
        var eventPageCalled = false

        composeTestRule.setContent {
            LoginScreen(
                AdminLogin = {},
                EventPage = {
                    eventPageCalled = true
                }
            )
        }

        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("Justin")

        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("hello")

        composeTestRule
            .onNodeWithText("Login")
            .performClick()

        assertTrue(eventPageCalled)
    }

    @Test
    fun adminLogin() {
        var adminLoginCalled = false

        composeTestRule.setContent {
            LoginScreen(
                AdminLogin = {
                    adminLoginCalled = true
                },
                EventPage = {}
            )
        }

        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("Victor")

        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("admin123")

        composeTestRule
            .onNodeWithText("Login")
            .performClick()

        assertTrue(adminLoginCalled)
    }

    @Test
    fun incorrectLogin() {
        composeTestRule.setContent {
            LoginScreen(
                AdminLogin = {},
                EventPage = {}
            )
        }

        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("Justin")

        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("wrong")

        composeTestRule
            .onNodeWithText("Login")
            .performClick()

        composeTestRule
            .onNodeWithText("Wrong username or password")
            .assertIsDisplayed()
    }
}
