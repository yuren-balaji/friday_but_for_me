package com.example.first_app_0_0_1.import

import android.content.Context
import android.net.Uri
import com.example.first_app_0_0_1.data.AppDatabase
import com.example.first_app_0_0_1.data.export.ExportedData
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader

class DataImportManager(private val context: Context) {

    suspend fun importDataFromJson(uri: Uri) {
        val db = AppDatabase.getDatabase(context)
        val json = readJsonFromFile(uri)
        val exportedData = Gson().fromJson(json, ExportedData::class.java)

        exportedData.notes.forEach { db.noteDao().insertNote(it) }
        exportedData.tasks.forEach { db.taskDao().insertTask(it) }
        exportedData.calendarEvents.forEach { db.calendarEventDao().insertEvent(it) }
    }

    private fun readJsonFromFile(uri: Uri): String {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use {
            val reader = BufferedReader(InputStreamReader(it))
            var line = reader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = reader.readLine()
            }
        }
        return stringBuilder.toString()
    }
}
