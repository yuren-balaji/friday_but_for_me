package com.example.first_app_0_0_1

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.first_app_0_0_1.automation.AutomationExecutor
import com.example.first_app_0_0_1.automation.VoiceCommand
import com.example.first_app_0_0_1.automation.VoiceCommandProcessor
import com.example.first_app_0_0_1.common.AppConstants
import com.example.first_app_0_0_1.data.AppDatabase
import com.example.first_app_0_0_1.data.CalendarEvent
import com.example.first_app_0_0_1.data.Note
import com.example.first_app_0_0_1.data.Task
import com.example.first_app_0_0_1.navigation.AppDestination
import com.example.first_app_0_0_1.screens.*
import com.example.first_app_0_0_1.tts.TextToSpeechManager
import com.example.first_app_0_0_1.ui.theme.First_app_0_0_1Theme
import com.example.first_app_0_0_1.viewmodels.*
import com.example.first_app_0_0_1.wakeword.WakeWordService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var onSpeechResult: ((String) -> Unit)? = null
    var isListening: Boolean by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) Log.e("MainActivity", "RECORD_AUDIO permission denied")
    }

    private val wakeWordReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AppConstants.ACTION_WAKE_WORD_DETECTED) {
                Log.d("MainActivity", "Wake word broadcast received!")
                startVoiceRecognition()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestAudioPermission()
        setupSpeechRecognizer()

        val serviceIntent = Intent(this, WakeWordService::class.java)
        startService(serviceIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(wakeWordReceiver, IntentFilter(AppConstants.ACTION_WAKE_WORD_DETECTED), RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(wakeWordReceiver, IntentFilter(AppConstants.ACTION_WAKE_WORD_DETECTED))
        }

        setContent {
            First_app_0_0_1Theme {
                PersonalAssistantApp(
                    speechRecognizer = speechRecognizer,
                    isListening = isListening,
                    onSpeechResultListener = { listener -> this.onSpeechResult = listener },
                    onListeningChanged = { listening -> this.isListening = listening },
                    onStartListening = { startVoiceRecognition() }
                )
            }
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        speechRecognizer?.startListening(intent)
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
        unregisterReceiver(wakeWordReceiver)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalAssistantApp(
    speechRecognizer: SpeechRecognizer?,
    isListening: Boolean,
    onSpeechResultListener: (((String) -> Unit) -> Unit),
    onListeningChanged: (Boolean) -> Unit,
    onStartListening: () -> Unit
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
    var showAddAutomationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(db, navController) {
        val commands = listOf(
            VoiceCommand("add note") { content, _, db ->
                if (content.isNotEmpty()) coroutineScope.launch { db.noteDao().insertNote(Note(title = content, content = "")) }
            },
            VoiceCommand("add task") { content, _, db ->
                if (content.isNotEmpty()) coroutineScope.launch { db.taskDao().insertTask(Task(title = content, description = "")) }
            },
            VoiceCommand("add event") { content, _, db ->
                if (content.isNotEmpty()) {
                    val today = Calendar.getInstance().timeInMillis
                    coroutineScope.launch { db.calendarEventDao().insertEvent(CalendarEvent(title = content, description = "", startTime = today, endTime = today, isAllDay = true)) }
                }
            },
            VoiceCommand("open notes") { _, navController, _ -> navController.navigate(AppDestination.NOTES.route) },
            VoiceCommand("open calendar") { _, navController, _ -> navController.navigate(AppDestination.CALENDAR.route) },
            VoiceCommand("open tasks") { _, navController, _ -> navController.navigate(AppDestination.TASKS.route) },
            VoiceCommand("open settings") { _, navController, _ -> navController.navigate(AppDestination.SETTINGS.route) },
            VoiceCommand("delete note") { title, _, db ->
                if (title.isNotEmpty()) {
                    coroutineScope.launch {
                        val notesToDelete = db.noteDao().findNotesByTitle(title)
                        notesToDelete.forEach { db.noteDao().deleteNote(it) }
                    }
                }
            },
            VoiceCommand("delete task") { title, _, db ->
                if (title.isNotEmpty()) {
                    coroutineScope.launch {
                        val tasksToDelete = db.taskDao().findTasksByTitle(title)
                        tasksToDelete.forEach { db.taskDao().deleteTask(it) }
                    }
                }
            },
            VoiceCommand("delete event") { title, _, db ->
                if (title.isNotEmpty()) {
                    coroutineScope.launch {
                        val eventsToDelete = db.calendarEventDao().findEventsByTitle(title)
                        eventsToDelete.forEach { db.calendarEventDao().deleteEvent(it) }
                    }
                }
            },
            VoiceCommand("complete task") { title, _, db ->
                if (title.isNotEmpty()) {
                    coroutineScope.launch {
                        val tasksToUpdate = db.taskDao().findTasksByTitle(title)
                        tasksToUpdate.forEach { db.taskDao().updateTask(it.copy(isCompleted = true)) }
                    }
                }
            }
        )
        VoiceCommandProcessor.registerCommands(commands)
    }

    DisposableEffect(Unit) {
        onSpeechResultListener { spokenText -> VoiceCommandProcessor.process(spokenText, navController, db) }
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
                                onStartListening()
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
                    AppDestination.AUTOMATION -> FloatingActionButton(onClick = { showAddAutomationDialog = true }) { Icon(Icons.Default.Add, null) }
                    else -> {}
                }
            }
        ) { padding ->
            AppNavHost(navController, Modifier.padding(padding), db)
        }
    }

    if (showAddNoteDialog) NoteEntryDialog(onDismiss = { showAddNoteDialog = false }, onAddNote = { t, c -> coroutineScope.launch { db.noteDao().insertNote(Note(title = t, content = c)) }; showAddNoteDialog = false })
    if (showAddEventDialog) EventEntryDialog(onDismiss = { showAddEventDialog = false }, onAddEvent = { t, d, s, e, isAllDay, loc -> coroutineScope.launch { db.calendarEventDao().insertEvent(CalendarEvent(title = t, description = d, startTime = s, endTime = e, isAllDay = isAllDay, location = loc)) }; showAddEventDialog = false })
    if (showAddTaskDialog) TaskEntryDialog(onDismiss = { showAddTaskDialog = false }, onAddTask = { t, d -> coroutineScope.launch { db.taskDao().insertTask(Task(title = t, description = d)) }; showAddTaskDialog = false })
    if (showAddAutomationDialog) AutomationEntryDialog(onDismiss = { showAddAutomationDialog = false }, onAddAutomation = { coroutineScope.launch { db.automationDao().insertAutomation(it) }; showAddAutomationDialog = false })
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier, db: AppDatabase) {
    val notes by db.noteDao().getAllNotes().collectAsState(initial = null)
    val events by db.calendarEventDao().getAllEvents().collectAsState(initial = null)
    val automations by db.automationDao().getAllAutomations().collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }
    val automationExecutor = remember(navController, db, ttsManager) { 
        AutomationExecutor(context, ttsManager, navController, db) 
    }

    LaunchedEffect(events, automations) {
        while (true) {
            val now = System.currentTimeMillis()
            
            // 1. Handle Event Reminders
            events?.let { eventList ->
                val upcomingEvents = eventList.filter { event ->
                    val timeDiff = event.startTime - now
                    timeDiff > 0 && timeDiff < TimeUnit.MINUTES.toMillis(15)
                }
                upcomingEvents.forEach { event -> ttsManager.speak("Upcoming event: ${event.title}") }
            }

            // 2. Handle Time-based Automations (Placeholder logic)
            // Real implementation would need precise scheduling or a background service
            automations?.filter { it.isActive && it.triggerType == "TIME" }?.forEach { automation ->
                // Check triggerConfig for scheduled time
            }

            delay(TimeUnit.MINUTES.toMillis(1))
        }
    }

    NavHost(navController, startDestination = AppDestination.CALENDAR.route, modifier = modifier) {
        composable(AppDestination.CALENDAR.route) {
            CalendarScreen(events, onEventDeleted = { scope.launch { db.calendarEventDao().deleteEvent(it) } })
        }
        composable(AppDestination.NOTES.route) {
            val searchViewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(db.noteDao(), context))
            NotesScreen(notes, searchViewModel, onNoteClick = { navController.navigate("noteDetail/$it") })
        }
        composable(AppDestination.TASKS.route) {
            val tasksViewModel: TasksViewModel = viewModel(factory = TasksViewModelFactory(db.taskDao()))
            TasksScreen(viewModel = tasksViewModel)
        }
        composable(AppDestination.GRAPH.route) {
            GraphScreen(notes ?: emptyList())
        }
        composable(AppDestination.AUTOMATION.route) {
            val automationViewModel: AutomationViewModel = viewModel(factory = AutomationViewModelFactory(db.automationDao()))
            AutomationScreen(
                viewModel = automationViewModel, 
                onAddAutomation = { /* Handled in PersonalAssistantApp */ },
                onRunAutomation = { automationExecutor.execute(it) }
            )
        }
        composable(AppDestination.MORE.route) { MoreScreen() }
        composable(AppDestination.SETTINGS.route) { SettingsScreen() }
        composable("noteDetail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            val vm: NoteDetailViewModel = viewModel(factory = NoteDetailViewModelFactory(db.noteDao(), noteId))
            NoteDetailScreen(viewModel = vm, onNavigateToNote = { navController.navigate("noteDetail/$it") }, onNavigateUp = { navController.navigateUp() })
        }
    }
}
