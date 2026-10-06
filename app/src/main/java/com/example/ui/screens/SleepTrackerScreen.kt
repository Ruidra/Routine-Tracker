package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SleepLog
import com.example.ui.viewmodel.RoutineViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SleepTrackerScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sleepLogs by viewModel.sleepLogs.collectAsStateWithLifecycle()

    val latestSleep = sleepLogs.firstOrNull()

    // Interactive add sleep log state
    var bedHour by remember { mutableIntStateOf(23) } // 11:00 PM
    var bedMinute by remember { mutableIntStateOf(0) }
    var wakeHour by remember { mutableIntStateOf(6) } // 6:30 AM
    var wakeMinute by remember { mutableIntStateOf(30) }
    var qualityRating by remember { mutableIntStateOf(5) }
    var sleepNotes by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }

    // Calculate duration in hours and minutes
    val totalMins = remember(bedHour, bedMinute, wakeHour, wakeMinute) {
        val bedTotal = bedHour * 60 + bedMinute
        var wakeTotal = wakeHour * 60 + wakeMinute
        if (wakeTotal <= bedTotal) {
            wakeTotal += 24 * 60 // next day
        }
        wakeTotal - bedTotal
    }
    val calculatedHrs = totalMins / 60
    val calculatedRemainingMins = totalMins % 60

    val avgSleepHours = remember(sleepLogs) {
        if (sleepLogs.isNotEmpty()) {
            val total = sleepLogs.take(7).sumOf { it.durationMinutes }
            (total / 60f) / sleepLogs.take(7).size
        } else 7.0f
    }

    fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayH = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayH, minute, amPm)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ঘুমের ট্র্যাকার ও সময় হিসাব",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "কখন ঘুমালেন এবং কত ঘণ্টা ঘুমালেন তা সহজেই ট্র্যাক করুন",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Hero Card: Last recorded sleep
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(0xFF1E1B4B) // Midnight deep indigo
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF4338CA), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "সর্বশেষ ঘুম (Last Sleep)",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA5B4FC)
                                )
                                Text(
                                    text = latestSleep?.dateStr ?: "আজকের তথ্য",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Sleep duration badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF312E81)
                        ) {
                            Text(
                                text = latestSleep?.formattedDuration ?: "৭h ১৫m",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF818CF8),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bedtime vs Wakeup Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Bedtime
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Nightlight, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ঘুমানোর সময়", fontSize = 11.sp, color = Color(0xFFC7D2FE))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = latestSleep?.formattedBedtime ?: "11:15 PM",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Divider / Arrow
                        Text("➔", fontSize = 20.sp, color = Color(0xFF6366F1), modifier = Modifier.align(Alignment.CenterVertically))

                        // Wake time
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("জেগে ওঠার সময়", fontSize = 11.sp, color = Color(0xFFC7D2FE))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = latestSleep?.formattedWakeTime ?: "06:30 AM",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sleep Target Progress
                    val sleepHours = latestSleep?.hoursFloat ?: 7.25f
                    val targetHours = 8.0f
                    val progress = (sleepHours / targetHours).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "দৈনিক ঘুমের লক্ষ্য: ৮ ঘণ্টা",
                            fontSize = 11.sp,
                            color = Color(0xFFA5B4FC)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f / 8.0 ঘণ্টা", sleepHours),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF818CF8),
                        trackColor = Color(0xFF312E81)
                    )
                }
            }
        }

        // Add / Log New Sleep Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛌 ঘুমের সময় যোগ করুন (Log Sleep)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (showAddForm) "লুকান" else "খুলুন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showAddForm = !showAddForm }
                        )
                    }

                    if (showAddForm) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Time pickers for Bedtime and Wake time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Bedtime Picker
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                bedHour = h
                                                bedMinute = m
                                            },
                                            bedHour,
                                            bedMinute,
                                            false
                                        ).show()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("ঘুমাতে গেছেন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatTime(bedHour, bedMinute),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Wakeup Picker
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                wakeHour = h
                                                wakeMinute = m
                                            },
                                            wakeHour,
                                            wakeMinute,
                                            false
                                        ).show()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("ঘুম থেকে উঠেছেন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatTime(wakeHour, wakeMinute),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Calculated Duration Alert
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("হিসাবকৃত মোট ঘুম:", fontSize = 13.sp)
                                Text(
                                    text = "${calculatedHrs} ঘণ্টা ${calculatedRemainingMins} মিনিট",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sleep Quality rating
                        Text(
                            text = "ঘুমের অনুভূতি (Sleep Quality):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { star ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "$star star",
                                    tint = if (star <= qualityRating) Color(0xFFFBBF24) else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { qualityRating = star }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Notes
                        OutlinedTextField(
                            value = sleepNotes,
                            onValueChange = { sleepNotes = it },
                            label = { Text("নোট / অনুভূতি") },
                            placeholder = { Text("কেমন ঘুম হলো? পড়াশোনায় সতেজতা?") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Save Button
                        Button(
                            onClick = {
                                val cal = Calendar.getInstance()
                                // Wakeup timestamp is today morning
                                val wakeCal = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, wakeHour)
                                    set(Calendar.MINUTE, wakeMinute)
                                    set(Calendar.SECOND, 0)
                                }
                                val bedCal = (wakeCal.clone() as Calendar).apply {
                                    set(Calendar.HOUR_OF_DAY, bedHour)
                                    set(Calendar.MINUTE, bedMinute)
                                    set(Calendar.SECOND, 0)
                                    if (bedHour > wakeHour || (bedHour == wakeHour && bedMinute >= wakeMinute)) {
                                        add(Calendar.DAY_OF_YEAR, -1) // Went to bed yesterday
                                    }
                                }

                                viewModel.addSleepRecord(
                                    bedtimeTimestamp = bedCal.timeInMillis,
                                    wakeTimeTimestamp = wakeCal.timeInMillis,
                                    qualityRating = qualityRating,
                                    notes = sleepNotes.trim()
                                )
                                showAddForm = false
                                sleepNotes = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_sleep_log_button")
                        ) {
                            Text("ঘুমের তথ্য সংরক্ষণ করুন (Save)")
                        }
                    }
                }
            }
        }

        // Average Sleep Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("গড় ঘুম (৭ দিন)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f ঘণ্টা", avgSleepHours),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ঘুমের স্থায়িত্ব", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (avgSleepHours >= 7f) "চমৎকার (Good)" else "কম (Short)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (avgSleepHours >= 7f) Color(0xFF16A34A) else Color(0xFFEA580C)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("রেকর্ড সংখ্যা", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${sleepLogs.size} দিন",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Sleep History Title
        item {
            Text(
                text = "ঘুমের পূর্ববর্তী ইতিহাস (Sleep History):",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Sleep History Items
        if (sleepLogs.isEmpty()) {
            item {
                Text(
                    text = "কোন ঘুমের ইতিহাস পাওয়া যায়নি",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(sleepLogs, key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.dateStr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${log.formattedBedtime} ➔ ${log.formattedWakeTime}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (log.notes.isNotBlank()) {
                                Text(
                                    text = log.notes,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Duration & Delete
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = log.formattedDuration,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteSleepRecord(log) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "মুছুন",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
