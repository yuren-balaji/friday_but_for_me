package com.example.first_app_0_0_1.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.first_app_0_0_1.data.CalendarEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    events: List<CalendarEvent>, 
    onEventDeleted: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = remember { Calendar.getInstance() }
    var currentMonth by rememberSaveable { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by rememberSaveable { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedDay by rememberSaveable { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Update calendar instance to current month/year
    calendar.set(Calendar.MONTH, currentMonth)
    calendar.set(Calendar.YEAR, currentYear)
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { 
                if (currentMonth == Calendar.JANUARY) {
                    currentMonth = Calendar.DECEMBER
                    currentYear--
                } else {
                    currentMonth--
                }
                selectedDay = null // Clear selection when month changes
            }) { Text("Previous") }
            Text(text = monthFormat.format(calendar.time), style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { 
                if (currentMonth == Calendar.DECEMBER) {
                    currentMonth = Calendar.JANUARY
                    currentYear++
                } else {
                    currentMonth++
                }
                selectedDay = null // Clear selection when month changes
            }) { Text("Next") }
        }

        // Days of the week header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { 
                Text(text = it, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            }
        }
        
        // Days of the month grid
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday, 1 for Monday, etc.

        LazyColumn(modifier = Modifier.weight(1f)) { // Use weight to allow events list below
            items((0 until (firstDayOfMonth + daysInMonth)).chunked(7)) { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { dayIndex ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                                .clickable { 
                                    if (dayIndex >= firstDayOfMonth) {
                                        selectedDay = dayIndex - firstDayOfMonth + 1
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayIndex >= firstDayOfMonth) {
                                val day = dayIndex - firstDayOfMonth + 1
                                Text(text = day.toString(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        // Display events for the selected day
        selectedDay?.let { day ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDayMillis = selectedCalendar.timeInMillis
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDayMillis = selectedCalendar.timeInMillis

            val eventsForSelectedDay = events.filter {
                it.startTime >= startOfDayMillis && it.startTime < endOfDayMillis &&
                (searchQuery.isEmpty() || 
                 it.title.contains(searchQuery, ignoreCase = true) ||
                 it.description.contains(searchQuery, ignoreCase = true) ||
                 it.location?.contains(searchQuery, ignoreCase = true) == true)
            }.sortedBy { it.startTime }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
                Text(
                    text = "Events for ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(startOfDayMillis))}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Events") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                if (eventsForSelectedDay.isEmpty()) {
                    Text(text = "No events found.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(eventsForSelectedDay, key = { it.id }) { event ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        onEventDeleted(event)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        when (dismissState.targetValue) {
                                            SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                                            else -> Color.Transparent
                                        }, label = "BackgroundColor"
                                    )
                                    val scale by animateFloatAsState(
                                        if (dismissState.targetValue ==  SwipeToDismissBoxValue.EndToStart) 1.2f else 1f, label = "IconScale"
                                    )

                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Event",
                                            modifier = Modifier.scale(scale)
                                        )
                                    }
                                },
                                content = { CalendarEventItem(event) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarEventItem(event: CalendarEvent) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleSmall)
            if (event.description.isNotEmpty()) {
                Text(text = event.description, style = MaterialTheme.typography.bodySmall)
            }
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startTime = timeFormat.format(Date(event.startTime))
            val endTime = timeFormat.format(Date(event.endTime))
            Text(text = "$startTime - $endTime", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            event.location?.let { 
                if (it.isNotEmpty()) {
                    Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
