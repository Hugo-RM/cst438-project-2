package com.cst438.project2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.cst438.project2.ui.LoginScreen
import com.cst438.project2.ui.SignUpScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val showSignUp = remember { mutableStateOf(false) }

            if (showSignUp.value) {
                SignUpScreen(onBackToLogin = { showSignUp.value = false })
            } else {
                LoginScreen(onSignUpClick = { showSignUp.value = true })
            }
        }
    }
}
