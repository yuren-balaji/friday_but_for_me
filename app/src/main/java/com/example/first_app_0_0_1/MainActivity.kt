package com.example.first_app_0_0_1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.first_app_0_0_1.data.AppDatabase
import com.example.first_app_0_0_1.data.CalendarEvent
import com.example.first_app_0_0_1.data.Note
import com.example.first_app_0_0_1.data.Task
import com.example.first_app_0_0_1.navigation.AppDestination
import com.example.first_app_0_0_1.screens.CalendarScreen
import com.example.first_app_0_0_1.screens.EventEntryDialog
import com.example.first_app_0_0_1.screens.MoreScreen
import com.example.first_app_0_0_1.screens.NoteDetailScreen
import com.example.first_app_0_0_1.screens.NoteEntryDialog
import com.example.first_app_0_0_1.screens.NotesScreen
import com.example.first_app_0_0_1.screens.SettingsScreen
import com.example.first_app_0_0_1.screens.TaskEntryDialog
import com.example.first_app_0_0_1.screens.TasksScreen
import com.example.first_app_0_0_1.ui.theme.First_app_0_0_1Theme
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var onSpeechResult: ((String) -> Unit)? = null
    var isListening: Boolean by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "RECORD_AUDIO permission granted")
        } else {
            Log.e("MainActivity", "RECORD_AUDIO permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestAudioPermission()
        setupSpeechRecognizer()

        setContent {
            First_app_0_0_1Theme {
                PersonalAssistantApp(
                    speechRecognizer = speechRecognizer,
                    isListening = isListening,
                    onSpeechResultListener = { listener ->
                        this.onSpeechResult = listener
                    },
                    onListeningChanged = { listening -> this.isListening = listening }
                )
            }
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening = true; Log.d("Speech", "onReadyForSpeech") }
            override fun onBeginningOfSpeech() { Log.d("Speech", "onBeginningOfSpeech") }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false; Log.d("Speech", "onEndOfSpeech") }
            override fun onError(error: Int) { isListening = false; Log.e("Speech", "onError: $error") }
            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)?.let {
                    spokenText -> onSpeechResult?.invoke(spokenText)
                }
                isListening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalAssistantApp(
    speechRecognizer: SpeechRecognizer?,
    isListening: Boolean,
    onSpeechResultListener: (((String) -> Unit) -> Unit),
    onListeningChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = AppDestination.fromString(currentBackStack?.destination?.route)

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val noteDao = db.noteDao()
    val calendarEventDao = db.calendarEventDao()
    val taskDao = db.taskDao()

    val coroutineScope = rememberCoroutineScope()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onSpeechResultListener { spokenText ->
            Log.d("Speech", "Result in Composable: $spokenText")
            // Simple voice command parsing
            when {
                spokenText.startsWith("add note", ignoreCase = true) -> {
                    val content = spokenText.substringAfter("add note ").trim()
                    if (content.isNotEmpty()) {
                        coroutineScope.launch { noteDao.insertNote(Note(title = content, content = "")) }
                    }
                }
                spokenText.startsWith("add task", ignoreCase = true) -> {
                    val content = spokenText.substringAfter("add task ").trim()
                    if (content.isNotEmpty()) {
                        coroutineScope.launch { taskDao.insertTask(Task(title = content, description = "")) }
                    }
                }
                spokenText.startsWith("add event", ignoreCase = true) -> {
                    val content = spokenText.substringAfter("add event ").trim()
                    if (content.isNotEmpty()) {
                        val today = Calendar.getInstance()
                        coroutineScope.launch { calendarEventDao.insertEvent(
                            CalendarEvent(
                                title = content, 
                                description = "", 
                                startTime = today.timeInMillis, 
                                endTime = today.timeInMillis, 
                                allDay = true
                            )
                        )}
                    }
                }
            }
        }
        onDispose {
            speechRecognizer?.stopListening()
            onListeningChanged(false)
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestination.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it.route == currentDestination.route,
                    onClick = { navController.navigate(it.route) { launchSingleTop = true } }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(title = { Text(currentDestination.label) },
                    actions = {
                        IconButton(onClick = {
                            if (isListening) {
                                speechRecognizer?.stopListening()
                                onListeningChanged(false)
                            } else {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) 
                                }
                                speechRecognizer?.startListening(intent)
                            }
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                )
            },
            floatingActionButton = {
                when (currentDestination) {
                    AppDestination.NOTES -> {
                        FloatingActionButton(onClick = { showAddNoteDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Note")
                        }
                    }
                    AppDestination.CALENDAR -> {
                        FloatingActionButton(onClick = { showAddEventDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Event")
                        }
                    }
                    AppDestination.TASKS -> {
                        FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Task")
                        }
                    }
                    else -> {}
                }
            }
        ) { innerPadding ->
            AppNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
        }
    }

    if (showAddNoteDialog) {
        NoteEntryDialog(
            onDismiss = { showAddNoteDialog = false },
            onAddNote = { title, content ->
                coroutineScope.launch { noteDao.insertNote(Note(title = title, content = content)) }
                showAddNoteDialog = false
            }
        )
    }

    if (showAddEventDialog) {
        EventEntryDialog(
            onDismiss = { showAddEventDialog = false },
            onAddEvent = { title, description, startTime, endTime, allDay, location ->
                coroutineScope.launch { calendarEventDao.insertEvent(CalendarEvent(title = title, description = description, startTime = startTime, endTime = endTime, allDay = allDay, location = location)) }
                showAddEventDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        TaskEntryDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, description ->
                coroutineScope.launch { taskDao.insertTask(Task(title = title, description = description)) }
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val noteDao = db.noteDao()
    val calendarEventDao = db.calendarEventDao()
    val taskDao = db.taskDao()

    val notes by noteDao.getAllNotes().collectAsState(initial = emptyList())
    val events by calendarEventDao.getAllEvents().collectAsState(initial = emptyList())
    val tasks by taskDao.getAllTasks().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = AppDestination.CALENDAR.route, modifier = modifier) {
        composable(AppDestination.CALENDAR.route) {
            CalendarScreen(events = events)
        }
        composable(AppDestination.NOTES.route) {
            NotesScreen(notes = notes, onNoteClick = { noteId -> navController.navigate("noteDetail/$noteId") })
        }
        composable(AppDestination.TASKS.route) {
            TasksScreen(tasks = tasks, onTaskUpdated = { task -> coroutineScope.launch { taskDao.updateTask(task) } })
        }
        composable(AppDestination.MORE.route) {
            MoreScreen()
        }
        composable(AppDestination.SETTINGS.route) {
            SettingsScreen()
        }
        composable("noteDetail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            if (noteId != null) {
                NoteDetailScreen(
                    noteId = noteId, 
                    noteDao = noteDao, 
                    onNoteUpdated = {
                        coroutineScope.launch { noteDao.updateNote(it) }
                        navController.popBackStack()
                    },
                    onNoteDeleted = {
                        coroutineScope.launch { noteDao.deleteNote(it) }
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    First_app_0_0_1Theme {
        PersonalAssistantApp(speechRecognizer = null, isListening = false, onSpeechResultListener = {}, onListeningChanged = {})
    }
}
