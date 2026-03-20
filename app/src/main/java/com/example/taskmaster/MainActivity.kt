package com.example.taskmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskmaster.core.di.AppContainer
import com.example.taskmaster.feature.auth.AuthViewModel
import com.example.taskmaster.feature.auth.AuthViewModelFactory
import com.example.taskmaster.feature.auth.SignInScreen
import com.example.taskmaster.feature.projects.ProjectsScreen
import com.example.taskmaster.feature.projects.ProjectsViewModel
import com.example.taskmaster.feature.projects.ProjectsViewModelFactory
import com.example.taskmaster.feature.tasks.TaskBoardScreen
import com.example.taskmaster.feature.tasks.TaskBoardViewModel
import com.example.taskmaster.feature.tasks.TaskBoardViewModelFactory
import com.example.taskmaster.navigation.AppRoutes
import com.example.taskmaster.ui.theme.TaskMasterTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val appContainer: AppContainer by lazy {
        AppContainer(activityProvider = { this })
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
                                launchSingleTop = true
                            }
                        }
                    } else {
                        if (navController.currentDestination?.route != AppRoutes.PROJECTS) {
                            navController.navigate(AppRoutes.PROJECTS) {
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
                    composable(AppRoutes.PROJECTS) {
                        val projectsViewModel: ProjectsViewModel = viewModel(
                            factory = ProjectsViewModelFactory(
                                authRepository = appContainer.authRepository,
                                projectRepository = appContainer.projectRepository
                            )
                        )
                        val projectsState by projectsViewModel.uiState.collectAsStateWithLifecycle()
                        ProjectsScreen(
                            uiState = projectsState,
                            onCreateProject = projectsViewModel::createProject,
                            onOpenProject = { project ->
                                navController.navigate(
                                    AppRoutes.taskBoard(
                                        projectId = project.id,
                                        projectName = project.name
                                    )
                                )
                            },
                            onSignOut = authViewModel::signOut
                        )
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
                                taskRepository = appContainer.taskRepository
                            )
                        )
                        val taskState by taskBoardViewModel.uiState.collectAsStateWithLifecycle()

                        TaskBoardScreen(
                            projectName = projectName,
                            uiState = taskState,
                            onAddTask = taskBoardViewModel::addTask,
                            onAdvanceTask = taskBoardViewModel::advanceStatus,
                            onSkipTask = taskBoardViewModel::skipTask,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
