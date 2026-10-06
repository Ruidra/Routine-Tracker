package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.RoutineRepository
import com.example.reminder.AlarmScheduler
import com.example.reminder.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class RoutineApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: RoutineRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = RoutineRepository(database.routineDao())

        NotificationHelper.createNotificationChannels(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.ensureDefaultSeedData()
                // Schedule alarms for all active routines
                val activeList = repository.activeRoutines.firstOrNull() ?: emptyList()
                activeList.forEach { routine ->
                    AlarmScheduler.scheduleRoutine(this@RoutineApplication, routine)
                }
            } catch (_: Exception) {}
        }
    }
}
