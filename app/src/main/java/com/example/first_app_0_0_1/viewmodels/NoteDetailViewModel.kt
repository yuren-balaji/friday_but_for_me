package com.example.first_app_0_0_1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.first_app_0_0_1.data.Note
import com.example.first_app_0_0_1.data.NoteDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteDetailViewModel(private val noteDao: NoteDao, private val noteId: String) : ViewModel() {

    val note: StateFlow<Note?> = noteDao.getNoteById(noteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateNote(title: String, content: String) {
        note.value?.let {
            val updatedNote = it.copy(title = title, content = content)
            viewModelScope.launch {
                noteDao.updateNote(updatedNote)
            }
        }
    }

    fun deleteNote() {
        note.value?.let {
            viewModelScope.launch {
                noteDao.deleteNote(it)
            }
        }
    }
}

class NoteDetailViewModelFactory(private val noteDao: NoteDao, private val noteId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteDetailViewModel(noteDao, noteId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
