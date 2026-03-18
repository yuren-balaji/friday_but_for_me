package com.example.first_app_0_0_1.automation

import androidx.navigation.NavController
import com.example.first_app_0_0_1.data.AppDatabase

/**
 * A simple processor that finds and executes the first matching voice command.
 */
object VoiceCommandProcessor {
    private val commands = mutableListOf<VoiceCommand>()

    /**
     * Registers a list of voice commands.
     */
    fun registerCommands(newCommands: List<VoiceCommand>) {
        commands.clear() // Clear existing commands before adding new ones
        commands.addAll(newCommands)
    }

    /**
     * Processes the spoken text to find and execute a matching command.
     *
     * @param spokenText The full text transcribed from the user's speech.
     * @param navController The NavController to use for navigation commands.
     * @param db The AppDatabase instance for data commands.
     */
    fun process(spokenText: String, navController: NavController, db: AppDatabase) {
        for (command in commands) {
            if (spokenText.startsWith(command.keyword, ignoreCase = true)) {
                val argument = spokenText.substringAfter(command.keyword).trim()
                command.action(argument, navController, db)
                // Stop after the first match
                return 
            }
        }
    }
}
