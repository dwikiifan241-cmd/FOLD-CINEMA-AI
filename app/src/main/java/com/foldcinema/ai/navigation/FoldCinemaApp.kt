package com.foldcinema.ai.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foldcinema.ai.data.ProjectViewModel
import com.foldcinema.ai.ui.adaptive.rememberWidthClass
import com.foldcinema.ai.ui.dashboard.DashboardScreen
import com.foldcinema.ai.ui.importvideo.ImportVideoScreen
import com.foldcinema.ai.ui.project.NewProjectScreen
import com.foldcinema.ai.ui.project.ProjectScreen
import com.foldcinema.ai.ui.settings.SettingsScreen
import com.foldcinema.ai.ui.splash.SplashScreen

@Composable
fun FoldCinemaApp(viewModel: ProjectViewModel = viewModel()) {
    val nav = rememberNavController()
    val widthClass = rememberWidthClass()

    NavHost(navController = nav, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    nav.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                widthClass = widthClass,
                projects = viewModel.projects,
                onNewProject = { nav.navigate(Routes.NEW_PROJECT) },
                onImportVideo = { nav.navigate(Routes.IMPORT_VIDEO) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onOpenProject = { project -> nav.navigate(Routes.project(project.id)) }
            )
        }

        composable(Routes.NEW_PROJECT) {
            NewProjectScreen(
                onBack = { nav.popBackStack() },
                onCreate = { name, resolution, fps ->
                    val project = viewModel.createProject(name, resolution, fps)
                    nav.navigate(Routes.project(project.id)) {
                        popUpTo(Routes.DASHBOARD)
                    }
                }
            )
        }

        composable(Routes.IMPORT_VIDEO) {
            ImportVideoScreen(
                widthClass = widthClass,
                onBack = { nav.popBackStack() },
                onCreateProject = { uri, info ->
                    val project = viewModel.createFromVideo(uri, info)
                    nav.navigate(Routes.project(project.id)) {
                        popUpTo(Routes.DASHBOARD)
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                widthClass = widthClass,
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = Routes.PROJECT,
            arguments = listOf(navArgument(Routes.PROJECT_ARG) { type = NavType.StringType })
        ) { entry ->
            val projectId = entry.arguments?.getString(Routes.PROJECT_ARG)
            ProjectScreen(
                widthClass = widthClass,
                project = viewModel.find(projectId),
                onBack = { nav.popBackStack() },
                onDelete = {
                    viewModel.delete(projectId)
                    nav.popBackStack()
                }
            )
        }
    }
}
