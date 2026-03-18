package com.example.first_app_0_0_1.automation

import android.content.Context
import android.util.Log
import androidx.navigation.NavController
import com.example.first_app_0_0_1.data.AppDatabase
import com.example.first_app_0_0_1.data.Automation
import com.example.first_app_0_0_1.tts.TextToSpeechManager

/**
 * Executes automation scripts. In this "Jarvis-like" phase, we start with simple
 * string-based command interpretation.
 */
class AutomationExecutor(
    private val context: Context,
    private val ttsManager: TextToSpeechManager,
    private val navController: NavController,
    private val db: AppDatabase
) {
    fun execute(automation: Automation) {
        if (!automation.isActive) return

        Log.d("AutomationExecutor", "Executing: ${automation.name}")
        
        // Simple script execution logic
        // For now, we support "speak: message", "log: message", and "command: voice_command"
        val lines = automation.script.lines()
        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                trimmedLine.startsWith("speak:", ignoreCase = true) -> {
                    val message = trimmedLine.substringAfter("speak:").trim()
                    ttsManager.speak(message)
                }
                trimmedLine.startsWith("log:", ignoreCase = true) -> {
                    val message = trimmedLine.substringAfter("log:").trim()
                    Log.i("AutomationOutput", "[${automation.name}]: $message")
                }
                trimmedLine.startsWith("command:", ignoreCase = true) -> {
                    val commandText = trimmedLine.substringAfter("command:").trim()
                    VoiceCommandProcessor.process(commandText, navController, db)
                }
            }
        }
    }
}
