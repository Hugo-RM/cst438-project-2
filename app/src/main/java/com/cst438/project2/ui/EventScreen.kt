
package com.cst438.project2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EventScreen(
    onHomeClick: () -> Unit,
    onCreateEventClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val selectedScreen = remember {
        mutableStateOf("Home")
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedScreen.value == "Home",
                    onClick = {
                        selectedScreen.value = "Home"
                        onHomeClick()
                    },
                    icon = {Text("⌂")},
                    label = {Text("Home")}
                )

                NavigationBarItem(
                    selected = selectedScreen.value == "Create Event",
                    onClick = {
                        selectedScreen.value= "Create Event"
                        onCreateEventClick()
                    },

                    icon = {Text("\uD83D\uDCD8")},
                    label = { Text("Create Event")}


                )

                NavigationBarItem(
                    selected = selectedScreen.value == "Profile",
                    onClick = {
                        selectedScreen.value = "Profile"
                        onProfileClick()
                    },
                    icon = { Text("👤") },
                    label = { Text("Profile") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onLogoutClick,
                    icon = {Text("↪")},
                    label = {Text("Log Out")}
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when(selectedScreen.value) {
                "Home" -> {
                    Text("Event")
                }

                "Create Event" -> {
                    CreateEventScreen()
                }

                "Profile" -> {
                    Text("Profile")
                }
            }



        }
    }
}