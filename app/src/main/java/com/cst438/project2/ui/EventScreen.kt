package com.cst438.project2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cst438.project2.auth.EventApi
import com.cst438.project2.auth.EventCategory
import com.cst438.project2.auth.EventItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventScreen(
    onHomeClick: () -> Unit,
    onCreateEventClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    accessToken: String?
) {
    var selectedScreen by remember { mutableStateOf("Home") }
    val eventApi = remember { EventApi() }
    var events by remember { mutableStateOf<List<EventItem>>(emptyList()) }
    var categories by remember { mutableStateOf<List<EventCategory>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var eventMessage by remember { mutableStateOf("") }
    var creatingEvent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            events = eventApi.getEvents()
            categories = eventApi.getCategories()
        } catch (error: Exception) {
            eventMessage = error.message ?: "Could not load events."
        }
    }


    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedScreen == "Home",
                    onClick = {
                        selectedScreen = "Home"
                        onHomeClick()
                    },
                    icon = {Text("⌂")},
                    label = {Text("Home")}
                )

                NavigationBarItem(
                    selected = selectedScreen== "Create Event",
                    onClick = {
                        selectedScreen = "Create Event"
                        onCreateEventClick()
                    },

                    icon = {Text("\uD83D\uDCD8")},
                    label = { Text("Create Event")}


                )

                NavigationBarItem(
                    selected = selectedScreen == "Profile",
                    onClick = {
                        selectedScreen = "Profile"
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
            when (selectedScreen) {
                "Home" -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top
                    ) {

                        Text(
                            text = "Events",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (eventMessage.isNotEmpty()) {
                            Text(text = eventMessage, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(events) { event ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = event.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = event.categoryName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(text = "Location: ${event.location}")
                                        Text(text = "Date: ${formatEventDate(event.startsAt)}")
                                        Text(
                                            text = "Time: ${formatEventTime(event.startsAt)} – ${formatEventTime(event.endsAt)}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                "Create Event" -> {
                    val categoryNames = if (categories.isNotEmpty()) categories.map { it.name } else listOf("Academic", "Social", "Sports", "Career", "Other")

                    CreateEventScreen(
                        categories = categoryNames,
                        onCreateClick = { title, description, category, location, eventDate, startTime, endTime ->
                            if (!creatingEvent) {
                                scope.launch {
                                    creatingEvent = true
                                    eventMessage = "Creating event..."

                                    try {
                                        val token = accessToken?.takeIf { it.isNotBlank() }

                                        if (token == null) {
                                            throw IllegalStateException(
                                                "Please sign in before creating an event"
                                            )
                                        }

                                        val selectedCategory = categories.find {
                                            it.name.equals(category, ignoreCase = true)
                                        } ?: categories.firstOrNull()

                                        if (selectedCategory == null) {
                                            throw IllegalStateException(
                                                "No categories available in database. Please ask an admin to add a category."
                                            )
                                        }

                                        eventApi.createEvent(
                                            title = title,
                                            description = description,
                                            categoryId = selectedCategory.id,
                                            location = location,
                                            eventDate = eventDate,
                                            startTime = startTime,
                                            endTime = endTime,
                                            accessToken = token
                                        )

                                        events = eventApi.getEvents()
                                        selectedScreen = "Home"
                                        eventMessage = ""
                                    } catch (error: Exception) {
                                        eventMessage = error.message ?: "Could not create the event."
                                    } finally {
                                        creatingEvent = false
                                    }
                                }
                            }
                        }
                    )
                }
                "Profile" -> {
                    Text("Profile")
                }
            }
        }
    }
}

private fun formatEventTime(value: String): String {
    val date = parseEventDateTime(value) ?: return value

    return SimpleDateFormat(
        "h:mm a", Locale.US).format(date)
}

private fun parseEventDateTime(value: String): Date? {
    if (value.length < 19) {
        return null
    }
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(value.substring(0, 19))
    } catch (error: Exception) {
        null
    }
}

private fun formatEventDate(value: String): String {
    val date = parseEventDateTime(value) ?: return value
    return SimpleDateFormat("MMMM d, yyyy", Locale.US).format(date)
}
