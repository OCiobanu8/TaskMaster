package com.example.taskmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskmaster.core.di.AppContainer
import com.example.taskmaster.feature.auth.AuthViewModel
import com.example.taskmaster.feature.auth.AuthViewModelFactory
import com.example.taskmaster.feature.auth.SignInScreen
import com.example.taskmaster.feature.overview.OverviewScreen
import com.example.taskmaster.feature.overview.OverviewViewModel
import com.example.taskmaster.feature.overview.OverviewViewModelFactory
import com.example.taskmaster.feature.projects.ProjectsScreen
import com.example.taskmaster.feature.projects.ProjectsViewModel
import com.example.taskmaster.feature.projects.ProjectsViewModelFactory
import com.example.taskmaster.feature.tasks.TaskBoardScreen
import com.example.taskmaster.feature.tasks.TaskBoardViewModel
import com.example.taskmaster.feature.tasks.TaskBoardViewModelFactory
import com.example.taskmaster.feature.tasks.TaskDetailsScreen
import com.example.taskmaster.feature.tasks.TaskDetailsViewModel
import com.example.taskmaster.feature.tasks.TaskDetailsViewModelFactory
import com.example.taskmaster.core.model.TaskStatus
import com.example.taskmaster.navigation.AppRoutes
import com.example.taskmaster.ui.theme.TaskMasterTheme
import com.google.firebase.FirebaseApp
import java.time.Instant
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val appContainer: AppContainer by lazy {
        AppContainer(
            activityProvider = { this },
            contentResolverProvider = { contentResolver }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            TaskMasterTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(appContainer.authRepository)
                )
                val authState by authViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(authState.currentUser?.id) {
                    if (authState.currentUser == null) {
                        if (navController.currentDestination?.route != AppRoutes.SIGN_IN) {
                            navController.navigate(AppRoutes.SIGN_IN) {
                                popUpTo(navController.graph.id)
                                launchSingleTop = true
                            }
                        }
                    } else {
                        if (navController.currentDestination?.route == AppRoutes.SIGN_IN) {
                            navController.navigate(AppRoutes.OVERVIEW) {
                                popUpTo(AppRoutes.SIGN_IN) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.SIGN_IN
                ) {
                    composable(AppRoutes.SIGN_IN) {
                        SignInScreen(
                            uiState = authState,
                            onSignInClick = authViewModel::signIn
                        )
                    }

                    composable(AppRoutes.OVERVIEW) {
                        val overviewViewModel: OverviewViewModel = viewModel(
                            factory = OverviewViewModelFactory(
                                authRepository = appContainer.authRepository,
                                taskRepository = appContainer.taskRepository,
                                calendarRepository = appContainer.calendarRepository
                            )
                        )
                        val overviewState by overviewViewModel.uiState.collectAsStateWithLifecycle()

                        AppDrawerScaffold(
                            title = "Today",
                            currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(AppRoutes.OVERVIEW) { saveState = true }
                                }
                            },
                            onSignOut = authViewModel::signOut
                        ) { padding ->
                            OverviewScreen(
                                uiState = overviewState,
                                contentPadding = padding,
                                onTaskClick = { task ->
                                    navController.navigate(
                                        AppRoutes.taskDetails(
                                            taskId = task.id,
                                            projectId = task.projectId,
                                            assigneeUserId = task.assigneeUserId,
                                            status = task.status.name,
                                            dueAtMillis = task.dueAt?.toEpochMilli(),
                                            createdAtMillis = task.createdAt?.toEpochMilli(),
                                            title = task.title
                                        )
                                    )
                                },
                                onCalendarPermissionResult = overviewViewModel::setCalendarPermissionGranted
                            )
                        }
                    }

                    composable(AppRoutes.PROJECTS) {
                        val projectsViewModel: ProjectsViewModel = viewModel(
                            factory = ProjectsViewModelFactory(
                                authRepository = appContainer.authRepository,
                                projectRepository = appContainer.projectRepository
                            )
                        )
                        val projectsState by projectsViewModel.uiState.collectAsStateWithLifecycle()

                        AppDrawerScaffold(
                            title = "Projects",
                            currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(AppRoutes.OVERVIEW) { saveState = true }
                                }
                            },
                            onSignOut = authViewModel::signOut
                        ) { padding ->
                            ProjectsScreen(
                                uiState = projectsState,
                                contentPadding = padding,
                                onCreateProject = projectsViewModel::createProject,
                                onOpenProject = { project ->
                                    navController.navigate(
                                        AppRoutes.taskBoard(
                                            projectId = project.id,
                                            projectName = project.name
                                        )
                                    )
                                }
                            )
                        }
                    }

                    composable(
                        route = AppRoutes.TASK_BOARD,
                        arguments = listOf(
                            navArgument("projectId") { type = NavType.StringType },
                            navArgument("projectName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val projectId = backStackEntry.arguments?.getString("projectId").orEmpty()
                        val projectName = backStackEntry.arguments?.getString("projectName").orEmpty()
                        val taskBoardViewModel: TaskBoardViewModel = viewModel(
                            key = "task-board-$projectId",
                            factory = TaskBoardViewModelFactory(
                                projectId = projectId,
                                authRepository = appContainer.authRepository,
                                projectRepository = appContainer.projectRepository,
                                taskRepository = appContainer.taskRepository
                            )
                        )
                        val taskState by taskBoardViewModel.uiState.collectAsStateWithLifecycle()

                        LaunchedEffect(taskState.projectDeleted) {
                            if (taskState.projectDeleted) {
                                navController.popBackStack()
                            }
                        }

                        AppDrawerScaffold(
                            title = projectName,
                            currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                            showBackButton = true,
                            onBack = { navController.popBackStack() },
                            topBarActions = {
                                IconButton(
                                    onClick = taskBoardViewModel::deleteProject,
                                    enabled = !taskState.deletingProject
                                ) {
                                    if (taskState.deletingProject) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete project"
                                        )
                                    }
                                }
                            },
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(AppRoutes.OVERVIEW) { saveState = true }
                                }
                            },
                            onSignOut = authViewModel::signOut
                        ) { padding ->
                            TaskBoardScreen(
                                contentPadding = padding,
                                uiState = taskState,
                                onAddTask = taskBoardViewModel::addTask,
                                onAdvanceTask = taskBoardViewModel::advanceStatus,
                                onSkipTask = taskBoardViewModel::skipTask
                            )
                        }
                    }

                    composable(AppRoutes.ACCOUNT) {
                        AppDrawerScaffold(
                            title = "Account",
                            currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(AppRoutes.OVERVIEW) { saveState = true }
                                }
                            },
                            onSignOut = authViewModel::signOut
                        ) { padding ->
                            PlaceholderScreen(
                                text = "Account management is ready for the next slice.",
                                padding = padding
                            )
                        }
                    }

                    composable(AppRoutes.SETTINGS) {
                        AppDrawerScaffold(
                            title = "Settings",
                            currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(AppRoutes.OVERVIEW) { saveState = true }
                                }
                            },
                            onSignOut = authViewModel::signOut
                        ) { padding ->
                            PlaceholderScreen(
                                text = "App settings will be added next.",
                                padding = padding
                            )
                        }
                    }

                    composable(
                        route = AppRoutes.TASK_DETAILS,
                        arguments = listOf(
                            navArgument("taskId") { type = NavType.StringType },
                            navArgument("projectId") { type = NavType.StringType },
                            navArgument("assigneeUserId") { type = NavType.StringType },
                            navArgument("status") { type = NavType.StringType },
                            navArgument("dueAtMillis") { type = NavType.LongType },
                            navArgument("createdAtMillis") { type = NavType.LongType },
                            navArgument("title") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
                        val projectId = backStackEntry.arguments?.getString("projectId").orEmpty()
                        val assigneeUserId = backStackEntry.arguments?.getString("assigneeUserId").orEmpty()
                        val status = backStackEntry.arguments?.getString("status").orEmpty()
                        val dueAtMillis = backStackEntry.arguments?.getLong("dueAtMillis") ?: -1L
                        val createdAtMillis = backStackEntry.arguments?.getLong("createdAtMillis") ?: -1L
                        val title = backStackEntry.arguments?.getString("title").orEmpty()
                        val dueAt = if (dueAtMillis > 0L) Instant.ofEpochMilli(dueAtMillis) else null
                        val createdAt = if (createdAtMillis > 0L) Instant.ofEpochMilli(createdAtMillis) else null
                        val taskDetailsViewModel: TaskDetailsViewModel = viewModel(
                            key = "task-details-$taskId",
                            factory = TaskDetailsViewModelFactory(
                                taskId = taskId,
                                initialStatus = TaskStatus.fromStorage(status),
                                taskRepository = appContainer.taskRepository
                            )
                        )
                        val taskDetailsState by taskDetailsViewModel.uiState.collectAsStateWithLifecycle()

                        LaunchedEffect(taskDetailsState.deleted) {
                            if (taskDetailsState.deleted) {
                                navController.popBackStack()
                            }
                        }

                        AppDrawerScaffold(
                            title = "Task Details",
                            currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                            showBackButton = true,
                            onBack = { navController.popBackStack() },
                            topBarActions = {
                                IconButton(
                                    onClick = taskDetailsViewModel::deleteTask,
                                    enabled = !taskDetailsState.deleting
                                ) {
                                    if (taskDetailsState.deleting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete task"
                                        )
                                    }
                                }
                            },
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(AppRoutes.OVERVIEW) { saveState = true }
                                }
                            },
                            onSignOut = authViewModel::signOut
                        ) { padding ->
                            TaskDetailsScreen(
                                contentPadding = padding,
                                taskId = taskId,
                                title = title,
                                projectId = projectId,
                                assigneeUserId = assigneeUserId,
                                dueAt = dueAt,
                                createdAt = createdAt,
                                status = taskDetailsState.currentStatus,
                                persistedStatus = taskDetailsState.persistedStatus,
                                savingStatus = taskDetailsState.savingStatus,
                                onStatusSelected = taskDetailsViewModel::selectStatus,
                                onSaveStatus = taskDetailsViewModel::saveStatus,
                                errorMessage = taskDetailsState.errorMessage
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class DrawerDestination(
    val label: String,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDrawerScaffold(
    title: String,
    currentDestination: NavDestination?,
    showBackButton: Boolean = false,
    onBack: (() -> Unit)? = null,
    topBarActions: @Composable RowScope.() -> Unit = {},
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val destinations = listOf(
        DrawerDestination("Overview", AppRoutes.OVERVIEW),
        DrawerDestination("Projects", AppRoutes.PROJECTS),
        DrawerDestination("Account", AppRoutes.ACCOUNT),
        DrawerDestination("Settings", AppRoutes.SETTINGS)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "TaskMaster",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                )

                destinations.forEach { destination ->
                    val isSelected = isRouteSelected(currentDestination?.route, destination.route)
                    NavigationDrawerItem(
                        label = { Text(destination.label) },
                        selected = isSelected,
                        onClick = {
                            onNavigate(destination.route)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                TextButton(
                    onClick = {
                        onSignOut()
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Text("Sign out")
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        if (showBackButton && onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Open menu"
                                )
                            }
                        }
                    },
                    actions = topBarActions
                )
            },
            content = content
        )
    }
}

@Composable
private fun PlaceholderScreen(text: String, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Text(text = text)
    }
}

private fun isRouteSelected(currentRoute: String?, expectedRoute: String): Boolean {
    return when {
        currentRoute == null -> false
        expectedRoute == AppRoutes.PROJECTS && currentRoute.startsWith("task_board/") -> true
        else -> currentRoute == expectedRoute
    }
}
