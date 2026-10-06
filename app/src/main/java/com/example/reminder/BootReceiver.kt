package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.RoutineApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val app = context.applicationContext as? RoutineApplication ?: return
            val repo = app.repository

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val routines = repo.activeRoutines.firstOrNull() ?: emptyList()
                    routines.forEach { routine ->
                        AlarmScheduler.scheduleRoutine(context, routine)
                    }
                } catch (_: Exception) {}
            }
        }
    }
}
