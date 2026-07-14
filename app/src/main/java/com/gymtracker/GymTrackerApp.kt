package com.gymtracker

import android.app.Application
import com.gymtracker.data.programs.ProgramSeedInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GymTrackerApp : Application() {

    @Inject
    lateinit var programSeedInitializer: ProgramSeedInitializer

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            programSeedInitializer.seedIfNeeded()
        }
    }
}
