package com.example.first_app_0_0_1.automation

import androidx.navigation.NavController
import com.example.first_app_0_0_1.data.AppDatabase

/**
 * Represents a voice command that the application can understand.
 *
 * @property keyword The trigger phrase for the command (e.g., "add note").
 * @property action The function to execute when the command is recognized. 
 * It receives the text spoken *after* the keyword, the NavController, and the AppDatabase instance.
 */
data class VoiceCommand(
    val keyword: String,
    val action: (String, NavController, AppDatabase) -> Unit
)
