package com.cst438.project2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cst438.project2.ui.LoginScreen
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun successfulLogin() {
        composeTestRule.setContent {
            LoginScreen()
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

        composeTestRule
            .onNodeWithText("Logging in")
            .assertIsDisplayed()
    }

    @Test
    fun incorrectLogin() {
        composeTestRule.setContent {
            LoginScreen()
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