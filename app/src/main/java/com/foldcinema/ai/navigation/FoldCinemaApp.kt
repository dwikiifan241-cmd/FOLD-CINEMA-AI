package com.foldcinema.ai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foldcinema.ai.FoldCinemaApplication
import com.foldcinema.ai.ui.adaptive.rememberWidthClass
import com.foldcinema.ai.ui.dashboard.DashboardScreen
import com.foldcinema.ai.ui.editor.EditorRoute
import com.foldcinema.ai.ui.importvideo.ImportVideoScreen
import com.foldcinema.ai.ui.project.NewProjectScreen
import com.foldcinema.ai.ui.settings.SettingsScreen
import com.foldcinema.ai.ui.splash.SplashScreen

@Composable
fun FoldCinemaApp() {
    val nav = rememberNavController()
    val widthClass = rememberWidthClass()
    val repository = (LocalContext.current.applicationContext as FoldCinemaApplication).repository

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
                projects = repository.projects,
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
                    val project = repository.createProject(name, resolution, fps)
                    nav.navigate(Routes.project(project.id)) {
                        popUpTo(Routes.DASHBOARD)
                    }
                }
            )
        }

        composable(Routes.IMPORT_VIDEO) {
            ImportVideoScreen(
                widthClass = widthClass,
                repository = repository,
                onBack = { nav.popBackStack() },
                onCreateProject = { assets ->
                    val project = repository.createProjectFromAssets(assets)
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
            EditorRoute(
                widthClass = widthClass,
                projectId = entry.arguments?.getString(Routes.PROJECT_ARG),
                repository = repository,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
