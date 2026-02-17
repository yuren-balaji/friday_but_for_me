package com.example.first_app_0_0_1.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.first_app_0_0_1.data.Note
import com.example.first_app_0_0_1.data.NoteDao

@Composable
fun NoteDetailScreen(
    noteId: String,
    noteDao: NoteDao,
    onNoteUpdated: (Note) -> Unit,
    onNoteDeleted: (Note) -> Unit
) {
    val noteState by noteDao.getNoteById(noteId).collectAsState(initial = null)

    noteState?.let { note ->
        var title by remember { mutableStateOf(note.title) }
        var content by remember { mutableStateOf(note.content) }

        // Update local state if the note from the database changes
        LaunchedEffect(note) {
            title = note.title
            content = note.content
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { 
                        val updatedNote = note.copy(title = title, content = content)
                        onNoteUpdated(updatedNote)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onNoteDeleted(note) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
