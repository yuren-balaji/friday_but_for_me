package com.example.first_app_0_0_1.data.export

import com.example.first_app_0_0_1.data.CalendarEvent
import com.example.first_app_0_0_1.data.Note
import com.example.first_app_0_0_1.data.Task

data class ExportedData(
    val notes: List<Note>,
    val tasks: List<Task>,
    val calendarEvents: List<CalendarEvent>
)
