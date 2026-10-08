package com.cst438.project2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.cst438.project2.ui.AdminScreen
import com.cst438.project2.ui.LoginScreen
import com.cst438.project2.ui.EventScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var showAdmin by remember {
                mutableStateOf(false)

            }

            var showEvent by remember {
                mutableStateOf(false)
            }
            if(showAdmin) {
                AdminScreen(logout = {
                    showAdmin = false
                })

            } else if(showEvent){
                EventScreen()

            } else {
                LoginScreen(
                    AdminLogin = {
                        showAdmin = true
                    },
                    EventPage = {
                        showEvent = true
                    }
                )
            }

        }
    }
}