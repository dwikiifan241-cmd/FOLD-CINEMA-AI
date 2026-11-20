package com.foldcinema.ai

import android.app.Application
import com.foldcinema.ai.data.ProjectRepository

class FoldCinemaApplication : Application() {

    lateinit var repository: ProjectRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ProjectRepository(this)
    }
}
