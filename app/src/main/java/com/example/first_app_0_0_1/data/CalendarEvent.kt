package com.example.first_app_0_0_1.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val isAllDay: Boolean = false,
    val location: String? = null
)
