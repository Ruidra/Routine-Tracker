package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActivityLog
import com.example.data.model.RoutineItem
import com.example.data.model.SleepLog
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    // --- Routine Items ---
    @Query("SELECT * FROM routine_items ORDER BY startHour ASC, startMinute ASC")
    fun getAllRoutines(): Flow<List<RoutineItem>>

    @Query("SELECT * FROM routine_items WHERE isEnabled = 1 ORDER BY startHour ASC, startMinute ASC")
    fun getActiveRoutines(): Flow<List<RoutineItem>>

    @Query("SELECT * FROM routine_items WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: Long): RoutineItem?

    @Query("SELECT * FROM routine_items WHERE id = :id LIMIT 1")
    fun getRoutineByIdFlow(id: Long): Flow<RoutineItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(item: RoutineItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(items: List<RoutineItem>): List<Long>

    @Update
    suspend fun updateRoutine(item: RoutineItem)

    @Delete
    suspend fun deleteRoutine(item: RoutineItem)

    @Query("DELETE FROM routine_items WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)

    @Query("SELECT COUNT(*) FROM routine_items")
    suspend fun getRoutineCount(): Int

    // --- Activity Completion Logs ---
    @Query("SELECT * FROM activity_logs WHERE dateStr = :dateStr ORDER BY completedAtTimestamp DESC")
    fun getLogsForDate(dateStr: String): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs ORDER BY completedAtTimestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE dateStr >= :startDate AND dateStr <= :endDate ORDER BY dateStr ASC")
    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLog): Long

    @Delete
    suspend fun deleteActivityLog(log: ActivityLog)

    @Query("DELETE FROM activity_logs WHERE routineId = :routineId AND dateStr = :dateStr")
    suspend fun deleteLogByRoutineAndDate(routineId: Long, dateStr: String)

    @Query("SELECT * FROM activity_logs WHERE routineId = :routineId AND dateStr = :dateStr LIMIT 1")
    suspend fun getLogByRoutineAndDate(routineId: Long, dateStr: String): ActivityLog?

    // --- Sleep Logs ---
    @Query("SELECT * FROM sleep_logs ORDER BY wakeTimeTimestamp DESC")
    fun getAllSleepLogs(): Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_logs WHERE dateStr = :dateStr LIMIT 1")
    fun getSleepLogForDate(dateStr: String): Flow<SleepLog?>

    @Query("SELECT * FROM sleep_logs ORDER BY wakeTimeTimestamp DESC LIMIT :limit")
    fun getRecentSleepLogs(limit: Int): Flow<List<SleepLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLog): Long

    @Delete
    suspend fun deleteSleepLog(log: SleepLog)
}
