package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.RoutineApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoutineAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val routineId = intent.getLongExtra("EXTRA_ID", -1L)
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "রুটিন শুরু"
        val categoryTitle = intent.getStringExtra("EXTRA_CATEGORY_TITLE") ?: "দৈনিক কাজ"
        val timeFormatted = intent.getStringExtra("EXTRA_TIME") ?: ""
        val leadMinutes = intent.getIntExtra("EXTRA_LEAD_MINUTES", 10)
        val soundPreference = intent.getStringExtra("EXTRA_SOUND") ?: "default"
        val vibrate = intent.getBooleanExtra("EXTRA_VIBRATE", true)

        if (routineId != -1L) {
            NotificationHelper.showRoutineReminder(
                context = context,
                routineId = routineId,
                title = title,
                categoryTitle = categoryTitle,
                timeFormatted = timeFormatted,
                leadMinutes = leadMinutes,
                soundPreference = soundPreference,
                vibrate = vibrate
            )

            // Reschedule next occurrence for this routine
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = context.applicationContext as? RoutineApplication
                    val repo = app?.repository
                    val routine = repo?.getRoutineById(routineId)
                    if (routine != null && routine.isEnabled) {
                        AlarmScheduler.scheduleRoutine(context, routine)
                    }
                } catch (_: Exception) {}
            }
        }
    }
}
