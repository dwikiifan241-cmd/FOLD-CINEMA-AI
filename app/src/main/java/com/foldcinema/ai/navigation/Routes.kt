package com.foldcinema.ai.navigation

object Routes {
    const val SPLASH = "splash"
    const val DASHBOARD = "dashboard"
    const val NEW_PROJECT = "new_project"
    const val IMPORT_VIDEO = "import_video"
    const val SETTINGS = "settings"
    const val PROJECT_ARG = "projectId"
    const val PROJECT = "project/{$PROJECT_ARG}"

    fun project(id: String) = "project/$id"
}
