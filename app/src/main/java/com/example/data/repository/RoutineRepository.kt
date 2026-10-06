package com.example.data.repository

import com.example.data.local.RoutineDao
import com.example.data.model.ActivityLog
import com.example.data.model.RoutineCategory
import com.example.data.model.RoutineItem
import com.example.data.model.SleepLog
import com.example.data.model.SoundPreference
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RoutineRepository(private val routineDao: RoutineDao) {

    val allRoutines: Flow<List<RoutineItem>> = routineDao.getAllRoutines()
    val activeRoutines: Flow<List<RoutineItem>> = routineDao.getActiveRoutines()
    val allLogs: Flow<List<ActivityLog>> = routineDao.getAllLogs()
    val allSleepLogs: Flow<List<SleepLog>> = routineDao.getAllSleepLogs()

    fun getLogsForDate(dateStr: String): Flow<List<ActivityLog>> =
        routineDao.getLogsForDate(dateStr)

    fun getSleepLogForDate(dateStr: String): Flow<SleepLog?> =
        routineDao.getSleepLogForDate(dateStr)

    fun getRecentSleepLogs(limit: Int = 14): Flow<List<SleepLog>> =
        routineDao.getRecentSleepLogs(limit)

    suspend fun getRoutineById(id: Long): RoutineItem? =
        routineDao.getRoutineById(id)

    suspend fun insertRoutine(item: RoutineItem): Long =
        routineDao.insertRoutine(item)

    suspend fun updateRoutine(item: RoutineItem) =
        routineDao.updateRoutine(item)

    suspend fun deleteRoutine(item: RoutineItem) =
        routineDao.deleteRoutine(item)

    suspend fun deleteRoutineById(id: Long) =
        routineDao.deleteRoutineById(id)

    suspend fun toggleRoutineCompletion(
        routine: RoutineItem,
        dateStr: String,
        isCompleted: Boolean
    ) {
        if (isCompleted) {
            val existing = routineDao.getLogByRoutineAndDate(routine.id, dateStr)
            if (existing == null) {
                routineDao.insertActivityLog(
                    ActivityLog(
                        routineId = routine.id,
                        category = routine.category,
                        title = routine.title,
                        dateStr = dateStr,
                        durationMinutes = routine.durationMinutes,
                        isCompleted = true,
                        completedAtTimestamp = System.currentTimeMillis(),
                        source = "routine"
                    )
                )
            }
        } else {
            routineDao.deleteLogByRoutineAndDate(routine.id, dateStr)
        }
    }

    suspend fun logTimerSession(
        title: String,
        category: RoutineCategory,
        durationMinutes: Int,
        routineId: Long? = null,
        notes: String = ""
    ) {
        val todayStr = getTodayDateStr()
        routineDao.insertActivityLog(
            ActivityLog(
                routineId = routineId,
                category = category.id,
                title = title,
                dateStr = todayStr,
                durationMinutes = durationMinutes,
                isCompleted = true,
                completedAtTimestamp = System.currentTimeMillis(),
                source = "timer",
                notes = notes
            )
        )
    }

    suspend fun insertSleepLog(
        bedtimeTimestamp: Long,
        wakeTimeTimestamp: Long,
        qualityRating: Int = 4,
        notes: String = ""
    ): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = sdf.format(Date(wakeTimeTimestamp))
        val durationMins = ((wakeTimeTimestamp - bedtimeTimestamp) / (1000 * 60)).coerceAtLeast(0).toInt()

        return routineDao.insertSleepLog(
            SleepLog(
                dateStr = dateStr,
                bedtimeTimestamp = bedtimeTimestamp,
                wakeTimeTimestamp = wakeTimeTimestamp,
                durationMinutes = durationMins,
                qualityRating = qualityRating,
                notes = notes
            )
        )
    }

    suspend fun deleteSleepLog(log: SleepLog) {
        routineDao.deleteSleepLog(log)
    }

    suspend fun deleteActivityLog(log: ActivityLog) {
        routineDao.deleteActivityLog(log)
    }

    suspend fun ensureDefaultSeedData() {
        val count = routineDao.getRoutineCount()
        if (count == 0) {
            // Seed default routines
            val defaults = listOf(
                RoutineItem(
                    title = "গণিত ও বিজ্ঞান প্রাইভেট",
                    category = RoutineCategory.PRIVATE_TUITION.id,
                    startHour = 6,
                    startMinute = 30,
                    endHour = 8,
                    endMinute = 0,
                    daysOfWeek = "1,2,3,4,5,6,7",
                    reminderMinutesBefore = 15,
                    soundPreference = SoundPreference.DIGITAL_ALARM.id,
                    notes = "অঙ্ক ও ফিজিক্স রিভিশন"
                ),
                RoutineItem(
                    title = "কলেজ ক্লাস ও ল্যাব",
                    category = RoutineCategory.COLLEGE.id,
                    startHour = 9,
                    startMinute = 30,
                    endHour = 13,
                    endMinute = 30,
                    daysOfWeek = "1,2,3,4,5,6", // Sat to Thu / Mon to Sat
                    reminderMinutesBefore = 15,
                    soundPreference = SoundPreference.ENERGETIC_BELL.id,
                    notes = "ক্লাস লেকচার ও ল্যাব প্র্যাকটিস"
                ),
                RoutineItem(
                    title = "দুপুরের খাবার ও বিশ্রাম",
                    category = RoutineCategory.REST.id,
                    startHour = 14,
                    startMinute = 0,
                    endHour = 15,
                    endMinute = 30,
                    daysOfWeek = "1,2,3,4,5,6,7",
                    reminderMinutesBefore = 5,
                    soundPreference = SoundPreference.GENTLE_CHIME.id,
                    notes = "খাবার, নামাজ ও পাওয়ার ন্যাপ"
                ),
                RoutineItem(
                    title = "ইংরেজি ও আইসিটি প্রাইভেট",
                    category = RoutineCategory.PRIVATE_TUITION.id,
                    startHour = 16,
                    startMinute = 30,
                    endHour = 18,
                    endMinute = 0,
                    daysOfWeek = "1,2,3,4,5,7",
                    reminderMinutesBefore = 10,
                    soundPreference = SoundPreference.DIGITAL_ALARM.id,
                    notes = "ইংরেজি গ্রামার ও আইসিটি প্রোগ্রামিং"
                ),
                RoutineItem(
                    title = "সন্ধ্যার গভীর পড়া (Self Study)",
                    category = RoutineCategory.SELF_STUDY.id,
                    startHour = 18,
                    startMinute = 30,
                    endHour = 21,
                    endMinute = 30,
                    daysOfWeek = "1,2,3,4,5,6,7",
                    reminderMinutesBefore = 10,
                    soundPreference = SoundPreference.CALM_HARP.id,
                    notes = "দৈনিক পড়া, হোমওয়ার্ক এবং নোট তৈরি"
                ),
                RoutineItem(
                    title = "রাতের খাবার ও বিনোদন",
                    category = RoutineCategory.REST.id,
                    startHour = 21,
                    startMinute = 30,
                    endHour = 22,
                    endMinute = 30,
                    daysOfWeek = "1,2,3,4,5,6,7",
                    reminderMinutesBefore = 5,
                    soundPreference = SoundPreference.GENTLE_CHIME.id,
                    notes = "পরিবারের সাথে আড্ডা ও রাতের খাবার"
                ),
                RoutineItem(
                    title = "ঘুমের প্রস্তুতি ও ঘুম",
                    category = RoutineCategory.SLEEP.id,
                    startHour = 23,
                    startMinute = 0,
                    endHour = 6,
                    endMinute = 0,
                    daysOfWeek = "1,2,3,4,5,6,7",
                    reminderMinutesBefore = 15,
                    soundPreference = SoundPreference.CALM_HARP.id,
                    notes = "৭ ঘণ্টার গভীর ঘুম"
                )
            )
            val routineIds = routineDao.insertRoutines(defaults)

            // Seed historical sleep logs for the past 5 days
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            for (i in 5 downTo 1) {
                cal.time = Date()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val dayStr = sdf.format(cal.time)

                // Bedtime: previous night 11:15 PM
                val bedCal = Calendar.getInstance().apply {
                    time = cal.time
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 15)
                    set(Calendar.SECOND, 0)
                }
                // Wake time: morning 6:15 AM
                val wakeCal = Calendar.getInstance().apply {
                    time = cal.time
                    set(Calendar.HOUR_OF_DAY, 6)
                    set(Calendar.MINUTE, 15 + (i * 5) % 20)
                    set(Calendar.SECOND, 0)
                }
                val durationMins = ((wakeCal.timeInMillis - bedCal.timeInMillis) / (1000 * 60)).toInt()
                routineDao.insertSleepLog(
                    SleepLog(
                        dateStr = dayStr,
                        bedtimeTimestamp = bedCal.timeInMillis,
                        wakeTimeTimestamp = wakeCal.timeInMillis,
                        durationMinutes = durationMins,
                        qualityRating = if (i % 2 == 0) 5 else 4,
                        notes = "নিয়মিত সময়মতো ঘুম"
                    )
                )

                // Seed some past completed activities
                routineDao.insertActivityLog(
                    ActivityLog(
                        routineId = routineIds.getOrNull(0),
                        category = RoutineCategory.PRIVATE_TUITION.id,
                        title = "গণিত ও বিজ্ঞান প্রাইভেট",
                        dateStr = dayStr,
                        durationMinutes = 90,
                        isCompleted = true,
                        source = "routine"
                    )
                )
                routineDao.insertActivityLog(
                    ActivityLog(
                        routineId = routineIds.getOrNull(1),
                        category = RoutineCategory.COLLEGE.id,
                        title = "কলেজ ক্লাস ও ল্যাব",
                        dateStr = dayStr,
                        durationMinutes = 240,
                        isCompleted = true,
                        source = "routine"
                    )
                )
                routineDao.insertActivityLog(
                    ActivityLog(
                        routineId = routineIds.getOrNull(4),
                        category = RoutineCategory.SELF_STUDY.id,
                        title = "সন্ধ্যার গভীর পড়া (Self Study)",
                        dateStr = dayStr,
                        durationMinutes = 180,
                        isCompleted = true,
                        source = "routine"
                    )
                )
            }
        }
    }

    companion object {
        fun getTodayDateStr(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun getCurrentDayOfWeek(): Int {
            // Calendar: SUNDAY=1, MONDAY=2, ... SATURDAY=7
            // Convert to 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
            val cal = Calendar.getInstance()
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
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
}
