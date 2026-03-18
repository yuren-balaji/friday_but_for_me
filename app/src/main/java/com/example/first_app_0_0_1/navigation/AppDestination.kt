package com.example.first_app_0_0_1.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
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
    GRAPH("graph", "Graph", Icons.Default.Hub),
    AUTOMATION("automation", "Automation", Icons.Default.AutoFixHigh),
    SETTINGS("settings", "Settings", Icons.Default.Settings),
    MORE("more", "More", Icons.Default.MoreHoriz);

    companion object {
        fun fromString(route: String?): AppDestination {
            return when (route) {
                "calendar" -> CALENDAR
                "notes" -> NOTES
                "tasks" -> TASKS
                "graph" -> GRAPH
                "automation" -> AUTOMATION
                "settings" -> SETTINGS
                "more" -> MORE
                else -> CALENDAR // Default destination
            }
        }
    }
}
