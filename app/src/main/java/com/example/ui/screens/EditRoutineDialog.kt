package com.example.ui.screens

import android.app.TimePickerDialog
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutineCategory
import com.example.data.model.RoutineItem
import com.example.data.model.SoundPreference
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditRoutineDialog(
    initialRoutine: RoutineItem?,
    onDismiss: () -> Unit,
    onSave: (RoutineItem) -> Unit,
    onDelete: ((RoutineItem) -> Unit)? = null
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialRoutine?.title ?: "") }
    var selectedCategory by remember {
        mutableStateOf(initialRoutine?.categoryEnum ?: RoutineCategory.SELF_STUDY)
    }
    var startHour by remember { mutableIntStateOf(initialRoutine?.startHour ?: 8) }
    var startMinute by remember { mutableIntStateOf(initialRoutine?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(initialRoutine?.endHour ?: 9) }
    var endMinute by remember { mutableIntStateOf(initialRoutine?.endMinute ?: 30) }

    val daysState = remember {
        val initialDays = initialRoutine?.daysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() }
            ?: listOf(1, 2, 3, 4, 5, 6, 7)
        mutableStateListOf<Int>().apply { addAll(initialDays) }
    }

    var reminderMinutes by remember {
        mutableIntStateOf(initialRoutine?.reminderMinutesBefore ?: 10)
    }
    var soundPref by remember {
        mutableStateOf(initialRoutine?.soundEnum ?: SoundPreference.DEFAULT)
    }
    var vibrate by remember { mutableStateOf(initialRoutine?.vibrate ?: true) }
    var isEnabled by remember { mutableStateOf(initialRoutine?.isEnabled ?: true) }
    var notes by remember { mutableStateOf(initialRoutine?.notes ?: "") }

    val quickTitles = when (selectedCategory) {
        RoutineCategory.PRIVATE_TUITION -> listOf("গণিত প্রাইভেট", "পদার্থবিজ্ঞান প্রাইভেট", "রসায়ন কোচিং", "ইংরেজি টিউটর")
        RoutineCategory.COLLEGE -> listOf("কলেজ ক্লাস", "থিওরি লেকচার", "ল্যাব প্র্যাকটিস", "কলেজ সেমিনার")
        RoutineCategory.SELF_STUDY -> listOf("নিজের পড়া (Self Study)", "ম্যাথ সলভিং", "টেস্ট পেপার রিভিশন", "হোমওয়ার্ক সম্পন্ন")
        RoutineCategory.REST -> listOf("দুপুরের বিশ্রাম ও ন্যাপ", "খাবার ও নামাজ", "হাঁটাহাঁটি ও চা", "পরিবারের সাথে আড্ডা")
        RoutineCategory.SLEEP -> listOf("রাতের ঘুম", "দুপুরের পাওয়ার ন্যাপ", "ঘুমের প্রস্তুতি")
        RoutineCategory.CUSTOM -> listOf("ব্যায়াম / জিম", "কুরআন পাঠ / প্রার্থনা", "প্রোগ্রামিং প্র্যাকটিস", "বই পড়া")
    }

    val dayNames = listOf(
        1 to "সোম (Mon)",
        2 to "মঙ্গল (Tue)",
        3 to "বুধ (Wed)",
        4 to "বৃহঃ (Thu)",
        5 to "শুক্র (Fri)",
        6 to "শনি (Sat)",
        7 to "রবি (Sun)"
    )

    fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialRoutine == null) "নতুন রুটিন যোগ করুন" else "রুটিন সম্পাদনা করুন",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Category Selector
                Text(
                    text = "ক্যাটাগরি নির্বাচন করুন (Category):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RoutineCategory.entries.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = category
                                if (title.isBlank()) {
                                    title = when (category) {
                                        RoutineCategory.PRIVATE_TUITION -> "গণিত প্রাইভেট"
                                        RoutineCategory.COLLEGE -> "কলেজ ক্লাস"
                                        RoutineCategory.SELF_STUDY -> "নিজের পড়া"
                                        RoutineCategory.REST -> "বিশ্রাম ও অবসর"
                                        RoutineCategory.SLEEP -> "ঘুমের সময়"
                                        RoutineCategory.CUSTOM -> "অন্যান্য কাজ"
                                    }
                                }
                            },
                            label = { Text(category.titleBn, fontSize = 12.sp) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(category.composeColor, CircleShape)
                                )
                            }
                        )
                    }
                }

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("রুটিনের নাম / বিষয় (Title)") },
                    placeholder = { Text("যেমন: গণিত প্রাইভেট, কলেজ লেকচার...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_title_input"),
                    singleLine = true
                )

                // Quick Title Suggestions
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    quickTitles.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { title = suggestion }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(suggestion, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Time Pickers (Start and End)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start Time Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        startHour = h
                                        startMinute = m
                                    },
                                    startHour,
                                    startMinute,
                                    false
                                ).show()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("শুরু (Start Time)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    formatTime(startHour, startMinute),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    // End Time Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        endHour = h
                                        endMinute = m
                                    },
                                    endHour,
                                    endMinute,
                                    false
                                ).show()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("শেষ (End Time)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    formatTime(endHour, endMinute),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                // Days of week
                Text(
                    text = "সপ্তাহের দিনসমূহ (Days of week):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dayNames.forEach { (dayInt, label) ->
                        val isChecked = daysState.contains(dayInt)
                        FilterChip(
                            selected = isChecked,
                            onClick = {
                                if (isChecked) {
                                    if (daysState.size > 1) daysState.remove(dayInt)
                                } else {
                                    daysState.add(dayInt)
                                }
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                // Reminder Lead Time
                Text(
                    text = "কতক্ষণ আগে নোটিফিকেশন দেবে? (Reminder Before):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        0 to "শুরুর সময় (On time)",
                        5 to "৫ মিনিট আগে (5m)",
                        10 to "১০ মিনিট আগে (10m)",
                        15 to "১৫ মিনিট আগে (15m)",
                        30 to "৩০ মিনিট আগে (30m)"
                    ).forEach { (mins, label) ->
                        FilterChip(
                            selected = reminderMinutes == mins,
                            onClick = { reminderMinutes = mins },
                            label = { Text(label, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }

                // Custom Sound Preference
                Text(
                    text = "অ্যালার্ম সাউন্ড প্রেফারেন্স (Sound Preference):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SoundPreference.entries.forEach { pref ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (soundPref == pref) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else Color.Transparent
                                )
                                .clickable { soundPref = pref }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (soundPref == pref) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pref.titleBn,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f),
                                fontWeight = if (soundPref == pref) FontWeight.Bold else FontWeight.Normal
                            )

                            // Preview tone button
                            if (pref != SoundPreference.SILENT) {
                                IconButton(
                                    onClick = {
                                        try {
                                            Thread {
                                                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
                                                toneGen.startTone(pref.toneType, 400)
                                                Thread.sleep(450)
                                                toneGen.release()
                                            }.start()
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "শব্দ শুনুন",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ভাইব্রেশন চালু রাখুন (Vibration)", fontSize = 14.sp)
                    Switch(checked = vibrate, onCheckedChange = { vibrate = it })
                }

                // Active Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("রুটিনটি সক্রিয় রাখুন (Active)", fontSize = 14.sp)
                    Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("অতিরিক্ত নোট (Notes / Topics)") },
                    placeholder = { Text("যেমন: চ্যাপ্টার ৪ রিভিশন, হোমওয়ার্ক...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val routineToSave = RoutineItem(
                            id = initialRoutine?.id ?: 0L,
                            title = title.trim(),
                            category = selectedCategory.id,
                            startHour = startHour,
                            startMinute = startMinute,
                            endHour = endHour,
                            endMinute = endMinute,
                            daysOfWeek = daysState.sorted().joinToString(","),
                            reminderMinutesBefore = reminderMinutes,
                            soundPreference = soundPref.id,
                            vibrate = vibrate,
                            isEnabled = isEnabled,
                            notes = notes.trim()
                        )
                        onSave(routineToSave)
                    }
                },
                modifier = Modifier.testTag("save_routine_button")
            ) {
                Text("সংরক্ষণ করুন (Save)")
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (initialRoutine != null && onDelete != null) {
                    TextButton(
                        onClick = { onDelete(initialRoutine) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("মুছুন")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("বাতিল (Cancel)")
                }
            }
        }
    )
}
