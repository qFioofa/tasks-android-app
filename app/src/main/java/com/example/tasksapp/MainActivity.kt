package com.example.tasksapp

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasksapp.data.local.TaskDatabase
import com.example.tasksapp.data.model.Task
import com.example.tasksapp.data.repository.SettingsRepository
import com.example.tasksapp.data.repository.TaskRepository
import com.example.tasksapp.ui.components.*
import com.example.tasksapp.ui.theme.TasksAppTheme
import com.example.tasksapp.ui.viewmodel.MainViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class TasksApplication : Application() {
    lateinit var taskRepository: TaskRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()

        val database = TaskDatabase.getDatabase(applicationContext)
        taskRepository = TaskRepository(database.taskDao())
        settingsRepository = SettingsRepository(applicationContext.dataStore)
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val application = applicationContext as TasksApplication
            val settingsRepository = application.settingsRepository

            val initialDarkTheme = runBlocking {
                settingsRepository.getTheme().first()
            }

            var isDarkTheme by remember { mutableStateOf(initialDarkTheme) }

            // Observe theme changes
            LaunchedEffect(Unit) {
                settingsRepository.getTheme().collect { theme ->
                    isDarkTheme = theme
                }
            }

            TasksAppTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TasksAppContent(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = {
                            isDarkTheme = !isDarkTheme
                            runBlocking {
                                settingsRepository.saveTheme(isDarkTheme)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksAppContent(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModel.Factory(
            taskRepository = (LocalContext.current.applicationContext as TasksApplication).taskRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showScrollToTop by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        showScrollToTop = listState.firstVisibleItemIndex > 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TasksHeader(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        },
        floatingActionButton = {
            ScrollToTopButton(
                visible = showScrollToTop,
                onClick = {
                    runBlocking {
                        listState.scrollToItem(0)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaskForm(
                onAddTask = { title, description ->
                    viewModel.addTask(title, description)
                },
                modifier = Modifier.fillMaxWidth()
            )

            TaskList(
                uiState = uiState,
                onToggleComplete = { task ->
                    viewModel.toggleTaskCompletion(task)
                },
                onDelete = { task ->
                    viewModel.deleteTask(task)
                },
                onEdit = { task ->
                    taskToEdit = task
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    taskToEdit?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { taskToEdit = null },
            onSave = { title, description ->
                viewModel.updateTask(task.copy(title = title, description = description))
                taskToEdit = null
            }
        )
    }
}
