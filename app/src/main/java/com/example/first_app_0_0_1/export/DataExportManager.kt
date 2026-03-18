package com.example.first_app_0_0_1.export

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.first_app_0_0_1.data.AppDatabase
import com.example.first_app_0_0_1.data.export.ExportedData
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.io.OutputStream

class DataExportManager(private val context: Context) {

    suspend fun exportDataToJson() {
        val db = AppDatabase.getDatabase(context)
        val notes = db.noteDao().getAllNotes().first()
        val tasks = db.taskDao().getAllTasks().first()
        val events = db.calendarEventDao().getAllEvents().first()

        val exportedData = ExportedData(notes, tasks, events)
        val json = Gson().toJson(exportedData)

        saveJsonToFile(json)
    }

    private fun saveJsonToFile(json: String) {
        val fileName = "personal_assistant_export_${System.currentTimeMillis()}.json"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        uri?.let {
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            outputStream?.use { stream ->
                stream.write(json.toByteArray())
            }
        }
    }
}
