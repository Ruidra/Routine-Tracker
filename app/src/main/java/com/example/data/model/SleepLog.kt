package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateStr: String, // Date corresponding to morning/waking day, e.g. "2026-10-06"
    val bedtimeTimestamp: Long,
    val wakeTimeTimestamp: Long,
    val durationMinutes: Int,
    val qualityRating: Int = 4, // 1 to 5
    val notes: String = ""
) {
    val formattedBedtime: String
        get() {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date(bedtimeTimestamp))
        }

    val formattedWakeTime: String
        get() {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date(wakeTimeTimestamp))
        }

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

    val hoursFloat: Float
        get() = durationMinutes / 60f
}
