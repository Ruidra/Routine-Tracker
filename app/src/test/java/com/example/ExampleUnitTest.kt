package com.example

import com.example.data.model.RoutineCategory
import com.example.data.model.RoutineItem
import com.example.data.model.SleepLog
import com.example.data.model.SoundPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testRoutineItemDurationCalculation() {
        val routineNormal = RoutineItem(
            title = "গণিত প্রাইভেট",
            category = RoutineCategory.PRIVATE_TUITION.id,
            startHour = 6,
            startMinute = 30,
            endHour = 8,
            endMinute = 0
        )
        assertEquals(90, routineNormal.durationMinutes)
        assertEquals("1h 30m", routineNormal.formattedDuration)

        val routineMidnight = RoutineItem(
            title = "রাতের ঘুম",
            category = RoutineCategory.SLEEP.id,
            startHour = 23,
            startMinute = 0,
            endHour = 6,
            endMinute = 0
        )
        assertEquals(420, routineMidnight.durationMinutes)
        assertEquals("7h", routineMidnight.formattedDuration)
    }

    @Test
    fun testCategoryEnumLookup() {
        assertEquals(RoutineCategory.COLLEGE, RoutineCategory.fromId("college"))
        assertEquals(RoutineCategory.PRIVATE_TUITION, RoutineCategory.fromId("private_tuition"))
        assertEquals(RoutineCategory.SELF_STUDY, RoutineCategory.fromId("self_study"))
        assertEquals(RoutineCategory.SLEEP, RoutineCategory.fromId("sleep"))
        assertEquals(RoutineCategory.REST, RoutineCategory.fromId("rest"))
        assertEquals(RoutineCategory.CUSTOM, RoutineCategory.fromId("unknown_category"))
    }

    @Test
    fun testSoundPreferenceLookup() {
        assertEquals(SoundPreference.DIGITAL_ALARM, SoundPreference.fromId("digital_alarm"))
        assertEquals(SoundPreference.GENTLE_CHIME, SoundPreference.fromId("gentle_chime"))
        assertEquals(SoundPreference.CALM_HARP, SoundPreference.fromId("calm_harp"))
        assertEquals(SoundPreference.DEFAULT, SoundPreference.fromId("unknown"))
    }

    @Test
    fun testSleepLogDuration() {
        val bedtime = 1700000000000L
        val waketime = bedtime + (7 * 3600 * 1000) + (30 * 60 * 1000) // 7h 30m
        val durationMins = ((waketime - bedtime) / (1000 * 60)).toInt()

        val sleepLog = SleepLog(
            dateStr = "2026-10-06",
            bedtimeTimestamp = bedtime,
            wakeTimeTimestamp = waketime,
            durationMinutes = durationMins
        )
        assertEquals(450, sleepLog.durationMinutes)
        assertEquals("7h 30m", sleepLog.formattedDuration)
        assertTrue(sleepLog.hoursFloat > 7.4f && sleepLog.hoursFloat < 7.6f)
    }
}
