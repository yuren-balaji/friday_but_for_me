package com.example.first_app_0_0_1.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.first_app_0_0_1.data.Note
import com.example.first_app_0_0_1.data.NoteDao
import com.example.first_app_0_0_1.search.SemanticSearch
import com.example.first_app_0_0_1.search.VectorDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchViewModel(private val noteDao: NoteDao, context: Context) : ViewModel() {

    private val semanticSearch = SemanticSearch(context)

    private val _searchResults = MutableStateFlow<List<Note>>(emptyList())
    val searchResults: StateFlow<List<Note>> = _searchResults.asStateFlow()

    fun performSearch(query: String) {
        viewModelScope.launch {
            // First, update the vector database with the current notes
            val allNotes = noteDao.getAllNotes().first()
            allNotes.forEach { note ->
                val vector = semanticSearch.getVector(note.title + "\n" + note.content)
                VectorDatabase.upsert(note.id, vector)
            }

            // Then, perform the semantic search
            val similarNoteIds = semanticSearch.findSimilar(query).map { it.first }
            val similarNotes = allNotes.filter { it.id in similarNoteIds }
            _searchResults.value = similarNotes
        }
    }
}

class SearchViewModelFactory(private val noteDao: NoteDao, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(noteDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
