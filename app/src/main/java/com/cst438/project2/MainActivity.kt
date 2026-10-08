package com.cst438.project2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.cst438.project2.ui.AdminScreen
import com.cst438.project2.ui.LoginScreen
import com.cst438.project2.ui.SignUpScreen
import com.cst438.project2.ui.EventScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val showAdmin = remember { mutableStateOf(false) }
            val showEvent = remember { mutableStateOf(false) }
            val showSignUp = remember { mutableStateOf(false) }

            if(showAdmin.value) {
                AdminScreen(logout = {
                    showAdmin.value = false
                })

            } else if(showEvent.value){
                EventScreen()


            } else if(showSignUp.value) {
                SignUpScreen(
                    onBackToLogin = {
                        showSignUp.value  = false
                    }
                )
            } else {
                LoginScreen(
                    AdminLogin = {
                        showAdmin.value = true
                    },
                    EventPage = {
                        showEvent.value = true
                    },
                    onSignUpClick = {
                        showSignUp.value = true
                    }
                )
            }
        }
    }
}
