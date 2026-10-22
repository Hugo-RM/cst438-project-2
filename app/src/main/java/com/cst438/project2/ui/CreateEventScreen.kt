package com.cst438.project2.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    categories: List<String> = listOf("Academic", "Social", "Sports", "Career", "Other"),
    onCreateClick: (
        title: String,
        description: String,
        category: String,
        location: String,
        eventDate: String,
        startTime: String,
        endTime: String) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }
    var formMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)

    ) {
        //Title input
        Text(
            text = "Create Event",
            style = MaterialTheme.typography.headlineMedium

        )


        OutlinedTextField(
            value = title,
            onValueChange = {title = it},
            label = {Text("Title")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )


        Text(
            text = "Please enter what is the event about",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = description,
            onValueChange ={ description = it },
            label = {Text("Description")},
            modifier = Modifier.fillMaxWidth(),
            minLines =3
        )

        //The dropdown menu
        ExposedDropdownMenuBox(
            expanded= categoryExpanded,
            onExpandedChange ={
                categoryExpanded = !categoryExpanded



            }

        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = {Text("Category")},
                placeholder = {Text("Select a category")},

                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = categoryExpanded
                    )

                },
                modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,

                onDismissRequest = {
                    categoryExpanded = false
                }

            ) {
                categories.forEach { item -> DropdownMenuItem(text = { Text(item) },
                        onClick = {
                            category = item
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Text(
            text = "Where would the event take place",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = location,
            onValueChange = {location = it},
            label = {Text("Location")},
            placeholder = {Text("Enter event location")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )




        //Date
        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        eventDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                    },

                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)

                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (eventDate.isEmpty()) {
                    "Select Event Date"
                } else {
                    "Date: $eventDate"
                }
            )
        }

        //time to go
        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance()

                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        startTime = String.format(
                            Locale.US,
                            "%02d:%02d",
                            hourOfDay,
                            minute
                        )
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (startTime.isEmpty()) {
                    "Select Start Time"
                } else {
                    "Start Time: ${formatTime(startTime)}"
                }
            )
        }

        //time it ends
        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance()

                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        endTime = String.format(Locale.US,
                            "%02d:%02d",
                            hour,
                            minute
                        )
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (endTime.isEmpty()) {
                    "Select End Time"
                } else {
                    "End Time: ${formatTime(endTime)}"
                }
            )
        }

        //fillout everything
        Button(
            onClick = {

                if(endTime <= startTime) {
                    formMessage = "End time must be later than start time"
                } else {
                    formMessage=""

                    onCreateClick(
                        title,
                        description,
                        category,
                        location,
                        eventDate,
                        startTime,
                        endTime

                    )

                }

            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() &&
                    description.isNotBlank() &&
                    category.isNotBlank() &&
                    eventDate.isNotBlank() &&
                    startTime.isNotBlank() &&
                    endTime.isNotBlank() &&
                    location.isNotBlank()
        ) {
            Text("Create Event")
        }
        if (formMessage.isNotBlank()) {
            Text(text = formMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun formatTime(time: String): String {
    val parts = time.split(":")
    val hour = parts[0].toInt()
    val minute = parts[1].toInt()

    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when (hour % 12) {
        0 -> 12
        else -> hour % 12
    }

    return String.format(
        Locale.US,
        "%d:%02d %s",
        displayHour,
        minute,
        period
    )
}


