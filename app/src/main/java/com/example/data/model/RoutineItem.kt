package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "routine_items")
data class RoutineItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = RoutineCategory.SELF_STUDY.id,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Mon .. 7=Sun
    val reminderMinutesBefore: Int = 10, // 0, 5, 10, 15, 30 min before
    val soundPreference: String = SoundPreference.DEFAULT.id,
    val vibrate: Boolean = true,
    val isEnabled: Boolean = true,
    val notes: String = ""
) {
    val categoryEnum: RoutineCategory
        get() = RoutineCategory.fromId(category)

    val soundEnum: SoundPreference
        get() = SoundPreference.fromId(soundPreference)

    val durationMinutes: Int
        get() {
            val startTotal = startHour * 60 + startMinute
            var endTotal = endHour * 60 + endMinute
            if (endTotal <= startTotal) {
                // cross midnight
                endTotal += 24 * 60
            }
            return endTotal - startTotal
        }

    val formattedDuration: String
        get() {
            val totalMins = durationMinutes
            val hrs = totalMins / 60
            val mins = totalMins % 60
            return when {
                hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
                hrs > 0 -> "${hrs}h"
                else -> "${mins}m"
            }
        }

    fun formattedTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
    }

    val startTimeFormatted: String
        get() = formattedTime(startHour, startMinute)

    val endTimeFormatted: String
        get() = formattedTime(endHour, endMinute)

    fun isActiveOnDay(dayOfWeek: Int): Boolean {
        // dayOfWeek: 1 = Mon .. 7 = Sun (Calendar.MONDAY is 2, etc, normalized to 1..7)
        val days = daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
        return days.contains(dayOfWeek)
    }
}
