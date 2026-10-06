package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.RoutineItem
import java.util.Calendar

object AlarmScheduler {

    fun scheduleRoutine(context: Context, routine: RoutineItem) {
        if (!routine.isEnabled) {
            cancelRoutine(context, routine.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nextTriggerTime = calculateNextTriggerTime(routine) ?: return

        val intent = Intent(context, RoutineAlarmReceiver::class.java).apply {
            putExtra("EXTRA_ID", routine.id)
            putExtra("EXTRA_TITLE", routine.title)
            putExtra("EXTRA_CATEGORY_TITLE", routine.categoryEnum.titleBn)
            putExtra("EXTRA_TIME", routine.startTimeFormatted)
            putExtra("EXTRA_LEAD_MINUTES", routine.reminderMinutesBefore)
            putExtra("EXTRA_SOUND", routine.soundPreference)
            putExtra("EXTRA_VIBRATE", routine.vibrate)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            routine.id.toInt(),
            intent,
            flags
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // In case exact alarm permission is not granted on Android 12+, use non-exact fallback
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        } catch (_: Exception) {}
    }

    fun cancelRoutine(context: Context, routineId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, RoutineAlarmReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            routineId.toInt(),
            intent,
            flags
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateNextTriggerTime(routine: RoutineItem): Long? {
        val now = Calendar.getInstance()
        val candidate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, routine.startHour)
            set(Calendar.MINUTE, routine.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Subtract reminder minutes
            add(Calendar.MINUTE, -routine.reminderMinutesBefore)
        }

        // Loop up to 7 days to find the next valid day of week
        for (dayOffset in 0..7) {
            val checkCal = (candidate.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }

            // Must be strictly in the future
            if (checkCal.timeInMillis > now.timeInMillis + 5000) {
                val dayOfWeek = toNormalizedDayOfWeek(checkCal.get(Calendar.DAY_OF_WEEK))
                if (routine.isActiveOnDay(dayOfWeek)) {
                    return checkCal.timeInMillis
                }
            }
        }
        return null
    }

    private fun toNormalizedDayOfWeek(calendarDay: Int): Int {
        // Calendar: SUNDAY=1, MONDAY=2, ... SATURDAY=7
        // Return: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
        return when (calendarDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
