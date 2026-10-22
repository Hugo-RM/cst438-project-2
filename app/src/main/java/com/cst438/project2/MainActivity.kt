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
            val showProfile = remember { mutableStateOf(false) }
            val showCreateEvent = remember { mutableStateOf(false) }
            val accessToken = remember { mutableStateOf<String?>(null) }

            if(showAdmin.value) {
                AdminScreen(accessToken = accessToken.value.orEmpty(), logout = {
                    showAdmin.value = false
                    accessToken.value = null

            })

            } else if(showEvent.value){
                EventScreen(
                    accessToken = accessToken.value,
                    onHomeClick = {
                        showCreateEvent.value = false
                        showProfile.value = false
                    },
                    onCreateEventClick = {
                        showCreateEvent.value = true
                        showProfile.value = false
                    },

                    onProfileClick = {
                        showProfile.value = true
                    },
                    onLogoutClick = {
                        showEvent.value = false
                        showCreateEvent.value = false
                        showProfile.value = false
                        accessToken.value = null

                    }

                )


            } else if(showSignUp.value) {
                SignUpScreen(
                    onBackToLogin = {
                        showSignUp.value  = false
                    }
                )
            } else {
                LoginScreen(

                    AdminLogin = {
                        adminToken -> accessToken.value = adminToken
                        showAdmin.value = true

                    },
                    EventPage = {
                        idToken -> accessToken.value = idToken
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
