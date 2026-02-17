package com.example.first_app_0_0_1.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEntryDialog(
    onDismiss: () -> Unit,
    onAddEvent: (String, String, Long, Long, Boolean, String?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var allDay by rememberSaveable { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState()
    val endDatePickerState = rememberDatePickerState()
    val endTimePickerState = rememberTimePickerState()

    var showStartDatePicker by rememberSaveable { mutableStateOf(false) }
    var showStartTimePicker by rememberSaveable { mutableStateOf(false) }
    var showEndDatePicker by rememberSaveable { mutableStateOf(false) }
    var showEndTimePicker by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Add New Event", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (Optional)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                // Start Date and Time Pickers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Start Date:")
                    Button(onClick = { showStartDatePicker = true }) { Text("Select Date") }
                    Text("Start Time:")
                    Button(onClick = { showStartTimePicker = true }) { Text("Select Time") }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // End Date and Time Pickers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("End Date:")
                    Button(onClick = { showEndDatePicker = true }) { Text("Select Date") }
                    Text("End Time:")
                    Button(onClick = { showEndTimePicker = true }) { Text("Select Time") }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { 
                    val startCalendar = Calendar.getInstance()
                    startDatePickerState.selectedDateMillis?.let { startCalendar.timeInMillis = it }
                    startCalendar.set(Calendar.HOUR_OF_DAY, startTimePickerState.hour)
                    startCalendar.set(Calendar.MINUTE, startTimePickerState.minute)
                    val startMillis = startCalendar.timeInMillis

                    val endCalendar = Calendar.getInstance()
                    endDatePickerState.selectedDateMillis?.let { endCalendar.timeInMillis = it }
                    endCalendar.set(Calendar.HOUR_OF_DAY, endTimePickerState.hour)
                    endCalendar.set(Calendar.MINUTE, endTimePickerState.minute)
                    val endMillis = endCalendar.timeInMillis

                    onAddEvent(title, description, startMillis, endMillis, allDay, location.ifEmpty { null })
                }) { Text("Add Event") }
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                Button(onClick = { showStartDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showStartTimePicker) {
        Dialog(onDismissRequest = { showStartTimePicker = false }) {
            TimeInput(state = startTimePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                Button(onClick = { showEndDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }

    if (showEndTimePicker) {
        Dialog(onDismissRequest = { showEndTimePicker = false }) {
            TimeInput(state = endTimePickerState)
        }
    }
}
