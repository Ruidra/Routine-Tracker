package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long? = null,
    val category: String,
    val title: String,
    val dateStr: String, // e.g. "2026-10-06"
    val durationMinutes: Int,
    val isCompleted: Boolean = true,
    val completedAtTimestamp: Long = System.currentTimeMillis(),
    val source: String = "routine", // "routine", "timer", "manual"
    val notes: String = ""
) {
    val categoryEnum: RoutineCategory
        get() = RoutineCategory.fromId(category)

    val formattedDuration: String
        get() {
            val hrs = durationMinutes / 60
            val mins = durationMinutes % 60
            return when {
                hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
                hrs > 0 -> "${hrs}h"
                else -> "${mins}m"
            }
        }
}
