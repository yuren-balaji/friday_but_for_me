package com.example.first_app_0_0_1.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    CALENDAR("calendar", "Calendar", Icons.Default.DateRange),
    NOTES("notes", "Notes", Icons.Default.Edit),
    TASKS("tasks", "Tasks", Icons.AutoMirrored.Filled.List),
    MORE("more", "More", Icons.Default.MoreHoriz),
    SETTINGS("settings", "Settings", Icons.Default.Settings);

    companion object {
        fun fromString(route: String?): AppDestination {
            return when (route) {
                "calendar" -> CALENDAR
                "notes" -> NOTES
                "tasks" -> TASKS
                "more" -> MORE
                "settings" -> SETTINGS
                else -> CALENDAR // Default destination
            }
        }
    }
}
