package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.RoutineApplication
import com.example.data.model.ActivityLog
import com.example.data.model.RoutineCategory
import com.example.data.model.RoutineItem
import com.example.data.model.SleepLog
import com.example.data.model.SoundPreference
import com.example.data.repository.RoutineRepository
import com.example.reminder.AlarmScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ActiveTimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isStopwatch: Boolean = false,
    val totalSeconds: Int = 25 * 60, // Default 25 min pomodoro
    val remainingSeconds: Int = 25 * 60,
    val elapsedSeconds: Int = 0,
    val routineId: Long? = null,
    val title: String = "নিজের পড়া (Self Study)",
    val category: RoutineCategory = RoutineCategory.SELF_STUDY,
    val notes: String = ""
)

data class DayHourStat(
    val dateStr: String,
    val dayLabel: String,
    val studyHours: Float,
    val collegeHours: Float,
    val privateHours: Float,
    val restHours: Float,
    val sleepHours: Float,
    val completionPercent: Int
)

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RoutineRepository = (application as RoutineApplication).repository
    private val context = application.applicationContext

    private val _selectedDate = MutableStateFlow(RoutineRepository.getTodayDateStr())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Database flows
    val allRoutines: StateFlow<List<RoutineItem>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRoutines: StateFlow<List<RoutineItem>> = repository.activeRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<ActivityLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sleepLogs: StateFlow<List<SleepLog>> = repository.allSleepLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered logs for selected date
    val currentDayLogs: StateFlow<List<ActivityLog>> = combine(allLogs, _selectedDate) { logs, date ->
        logs.filter { it.dateStr == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active timer state
    private val _timerState = MutableStateFlow(ActiveTimerState())
    val timerState: StateFlow<ActiveTimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Observe and update routines if empty
        viewModelScope.launch {
            repository.ensureDefaultSeedData()
        }
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    // --- Routine CRUD Actions ---
    fun saveRoutine(routine: RoutineItem) {
        viewModelScope.launch {
            val id = if (routine.id == 0L) {
                repository.insertRoutine(routine)
            } else {
                repository.updateRoutine(routine)
                routine.id
            }
            // Update alarm
            val updatedItem = routine.copy(id = id)
            if (updatedItem.isEnabled) {
                AlarmScheduler.scheduleRoutine(context, updatedItem)
            } else {
                AlarmScheduler.cancelRoutine(context, updatedItem.id)
            }
        }
    }

    fun toggleRoutineEnabled(routine: RoutineItem, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = routine.copy(isEnabled = isEnabled)
            repository.updateRoutine(updated)
            if (isEnabled) {
                AlarmScheduler.scheduleRoutine(context, updated)
            } else {
                AlarmScheduler.cancelRoutine(context, updated.id)
            }
        }
    }

    fun deleteRoutine(routine: RoutineItem) {
        viewModelScope.launch {
            AlarmScheduler.cancelRoutine(context, routine.id)
            repository.deleteRoutine(routine)
        }
    }

    // --- Daily Completion Toggle ---
    fun toggleRoutineCompletion(routine: RoutineItem, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleRoutineCompletion(
                routine = routine,
                dateStr = _selectedDate.value,
                isCompleted = isCompleted
            )
        }
    }

    // --- Sleep Tracker Actions ---
    fun addSleepRecord(
        bedtimeTimestamp: Long,
        wakeTimeTimestamp: Long,
        qualityRating: Int = 4,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertSleepLog(
                bedtimeTimestamp = bedtimeTimestamp,
                wakeTimeTimestamp = wakeTimeTimestamp,
                qualityRating = qualityRating,
                notes = notes
            )
        }
    }

    fun deleteSleepRecord(sleepLog: SleepLog) {
        viewModelScope.launch {
            repository.deleteSleepLog(sleepLog)
        }
    }

    // --- Timer Actions ---
    fun setupTimerForRoutine(routine: RoutineItem) {
        stopTimer()
        val durationSecs = (routine.durationMinutes * 60).coerceAtLeast(60)
        _timerState.value = ActiveTimerState(
            isRunning = false,
            isPaused = false,
            isStopwatch = false,
            totalSeconds = durationSecs,
            remainingSeconds = durationSecs,
            elapsedSeconds = 0,
            routineId = routine.id,
            title = routine.title,
            category = routine.categoryEnum,
            notes = routine.notes
        )
        _selectedTab.value = 1 // Switch to Timer tab
    }

    fun setupCustomTimer(
        title: String,
        category: RoutineCategory,
        minutes: Int,
        isStopwatch: Boolean
    ) {
        stopTimer()
        val totalSec = minutes * 60
        _timerState.value = ActiveTimerState(
            isRunning = false,
            isPaused = false,
            isStopwatch = isStopwatch,
            totalSeconds = totalSec,
            remainingSeconds = totalSec,
            elapsedSeconds = 0,
            routineId = null,
            title = title,
            category = category
        )
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return
        _timerState.value = _timerState.value.copy(isRunning = true, isPaused = false)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.isRunning) {
                delay(1000L)
                val curr = _timerState.value
                if (!curr.isRunning || curr.isPaused) break

                if (curr.isStopwatch) {
                    _timerState.value = curr.copy(
                        elapsedSeconds = curr.elapsedSeconds + 1
                    )
                } else {
                    if (curr.remainingSeconds > 1) {
                        _timerState.value = curr.copy(
                            remainingSeconds = curr.remainingSeconds - 1,
                            elapsedSeconds = curr.elapsedSeconds + 1
                        )
                    } else {
                        // Timer completed!
                        _timerState.value = curr.copy(
                            remainingSeconds = 0,
                            elapsedSeconds = curr.totalSeconds,
                            isRunning = false,
                            isPaused = false
                        )
                        // Auto log session
                        finishAndLogTimer()
                        break
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isRunning = false, isPaused = true)
        timerJob?.cancel()
    }

    fun stopTimer() {
        timerJob?.cancel()
        val curr = _timerState.value
        _timerState.value = curr.copy(
            isRunning = false,
            isPaused = false,
            remainingSeconds = curr.totalSeconds,
            elapsedSeconds = 0
        )
    }

    fun finishAndLogTimer() {
        val curr = _timerState.value
        val loggedMins = (curr.elapsedSeconds / 60).coerceAtLeast(1)
        viewModelScope.launch {
            repository.logTimerSession(
                title = curr.title,
                category = curr.category,
                durationMinutes = loggedMins,
                routineId = curr.routineId,
                notes = "টাইমার দিয়ে সম্পন্ন (${loggedMins} মিনিট)"
            )
            // Reset timer
            stopTimer()
        }
    }

    // --- Statistics Helper Computations ---
    fun getCategoryHoursForDate(
        category: RoutineCategory,
        logs: List<ActivityLog>,
        sleepList: List<SleepLog>,
        dateStr: String
    ): Float {
        if (category == RoutineCategory.SLEEP) {
            val sleepForDate = sleepList.find { it.dateStr == dateStr }
            return (sleepForDate?.durationMinutes ?: 0) / 60f
        }
        val mins = logs.filter { it.dateStr == dateStr && it.category == category.id }
            .sumOf { it.durationMinutes }
        return mins / 60f
    }

    fun getWeekDayStats(
        routines: List<RoutineItem>,
        logs: List<ActivityLog>,
        sleepList: List<SleepLog>
    ): List<DayHourStat> {
        val list = mutableListOf<DayHourStat>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dStr = sdf.format(c.time)
            val dLabel = if (i == 0) "Today" else dayFormat.format(c.time)

            val studyH = getCategoryHoursForDate(RoutineCategory.SELF_STUDY, logs, sleepList, dStr)
            val collegeH = getCategoryHoursForDate(RoutineCategory.COLLEGE, logs, sleepList, dStr)
            val privateH = getCategoryHoursForDate(RoutineCategory.PRIVATE_TUITION, logs, sleepList, dStr)
            val restH = getCategoryHoursForDate(RoutineCategory.REST, logs, sleepList, dStr)
            val sleepH = getCategoryHoursForDate(RoutineCategory.SLEEP, logs, sleepList, dStr)

            // Day completion rate
            val dayOfWeek = when (c.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }
            val plannedForDay = routines.filter { it.isEnabled && it.isActiveOnDay(dayOfWeek) }
            val completedForDay = logs.filter { it.dateStr == dStr }.distinctBy { it.routineId ?: it.id }

            val percent = if (plannedForDay.isNotEmpty()) {
                ((completedForDay.size.toFloat() / plannedForDay.size.toFloat()) * 100).toInt().coerceAtMost(100)
            } else if (completedForDay.isNotEmpty()) {
                100
            } else {
                0
            }

            list.add(
                DayHourStat(
                    dateStr = dStr,
                    dayLabel = dLabel,
                    studyHours = studyH,
                    collegeHours = collegeH,
                    privateHours = privateH,
                    restHours = restH,
                    sleepHours = sleepH,
                    completionPercent = percent
                )
            )
        }
        return list
    }
}
