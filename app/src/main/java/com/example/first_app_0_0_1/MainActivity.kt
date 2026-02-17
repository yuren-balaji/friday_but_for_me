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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.first_app_0_0_1.data.*
import com.example.first_app_0_0_1.navigation.AppDestination
import com.example.first_app_0_0_1.screens.*
import com.example.first_app_0_0_1.ui.theme.First_app_0_0_1Theme
import com.example.first_app_0_0_1.viewmodels.NoteDetailViewModel
import com.example.first_app_0_0_1.viewmodels.NoteDetailViewModelFactory
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var onSpeechResult: ((String) -> Unit)? = null
    var isListening: Boolean by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) Log.e("MainActivity", "RECORD_AUDIO permission denied")
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
                    onSpeechResultListener = { listener -> this.onSpeechResult = listener },
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
            override fun onReadyForSpeech(params: Bundle?) { isListening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) { isListening = false }
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
    val coroutineScope = rememberCoroutineScope()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onSpeechResultListener { spokenText ->
            when {
                spokenText.startsWith("add note", ignoreCase = true) -> {
                    val content = spokenText.substringAfter("add note ").trim()
                    if (content.isNotEmpty()) coroutineScope.launch { db.noteDao().insertNote(Note(title = content, content = "")) }
                }
                spokenText.startsWith("add task", ignoreCase = true) -> {
                    val content = spokenText.substringAfter("add task ").trim()
                    if (content.isNotEmpty()) coroutineScope.launch { db.taskDao().insertTask(Task(title = content, description = "")) }
                }
                spokenText.startsWith("add event", ignoreCase = true) -> {
                    val content = spokenText.substringAfter("add event ").trim()
                    if (content.isNotEmpty()) {
                        val today = Calendar.getInstance().timeInMillis
                        coroutineScope.launch { db.calendarEventDao().insertEvent(CalendarEvent(title = content, description = "", startTime = today, endTime = today, allDay = true)) }
                    }
                }
            }
        }
        onDispose { speechRecognizer?.stopListening() }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestination.entries.forEach { dest ->
                item(
                    icon = { Icon(dest.icon, contentDescription = dest.label) },
                    label = { Text(dest.label) },
                    selected = dest.route == currentDestination.route,
                    onClick = { navController.navigate(dest.route) { launchSingleTop = true } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentDestination.label) },
                    actions = {
                        IconButton(onClick = {
                            if (isListening) {
                                speechRecognizer?.stopListening()
                                onListeningChanged(false)
                            } else {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                }
                                speechRecognizer?.startListening(intent)
                            }
                        }) {
                            Icon(Icons.Default.Mic, tint = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, contentDescription = null)
                        }
                    }
                )
            },
            floatingActionButton = {
                when (currentDestination) {
                    AppDestination.NOTES -> FloatingActionButton(onClick = { showAddNoteDialog = true }) { Icon(Icons.Default.Add, null) }
                    AppDestination.CALENDAR -> FloatingActionButton(onClick = { showAddEventDialog = true }) { Icon(Icons.Default.Add, null) }
                    AppDestination.TASKS -> FloatingActionButton(onClick = { showAddTaskDialog = true }) { Icon(Icons.Default.Add, null) }
                    else -> {}
                }
            }
        ) { padding ->
            AppNavHost(navController, Modifier.padding(padding), db)
        }
    }

    if (showAddNoteDialog) NoteEntryDialog(onDismiss = { showAddNoteDialog = false }, onAddNote = { t, c -> coroutineScope.launch { db.noteDao().insertNote(Note(title = t, content = c)) }; showAddNoteDialog = false })
    if (showAddEventDialog) EventEntryDialog(
        onDismiss = { showAddEventDialog = false },
        onAddEvent = { t, d, s, e, a, l ->
            coroutineScope.launch {
                db.calendarEventDao().insertEvent(
                    CalendarEvent(
                        title = t,
                        description = d,
                        startTime = s,
                        endTime = e,
                        allDay = a,
                        location = l
                    )
                )
            }
            showAddEventDialog = false
        }
    )

    if (showAddTaskDialog) TaskEntryDialog(onDismiss = { showAddTaskDialog = false }, onAddTask = { t, d -> coroutineScope.launch { db.taskDao().insertTask(Task(title = t, description = d)) }; showAddTaskDialog = false })
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier, db: AppDatabase) {
    val notes by db.noteDao().getAllNotes().collectAsState(initial = emptyList())
    val events by db.calendarEventDao().getAllEvents().collectAsState(initial = emptyList())
    val tasks by db.taskDao().getAllTasks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    NavHost(navController, startDestination = AppDestination.CALENDAR.route, modifier = modifier) {
        composable(AppDestination.CALENDAR.route) {
            CalendarScreen(events, onEventDeleted = { scope.launch { db.calendarEventDao().deleteEvent(it) } })
        }
        composable(AppDestination.NOTES.route) {
            NotesScreen(notes, onNoteClick = { navController.navigate("noteDetail/$it") })
        }
        composable(AppDestination.TASKS.route) {
            TasksScreen(
                tasks = tasks,
                onTaskDeleted = { scope.launch { db.taskDao().deleteTask(it) } },
                onTaskStatusChanged = { task, isDone ->
                    scope.launch { db.taskDao().updateTask(task.copy(isCompleted = isDone)) }
                }
            )
        }
        composable(AppDestination.MORE.route) { MoreScreen() }
        composable(AppDestination.SETTINGS.route) { SettingsScreen() }
        composable("noteDetail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            val vm: NoteDetailViewModel = viewModel(factory = NoteDetailViewModelFactory(db.noteDao(), noteId))
            NoteDetailScreen(vm, onNavigateUp = { navController.navigateUp() })
        }
    }
}
