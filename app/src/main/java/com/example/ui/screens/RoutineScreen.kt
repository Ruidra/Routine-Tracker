package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RoutineCategory
import com.example.data.model.RoutineItem
import com.example.data.repository.RoutineRepository
import com.example.ui.viewmodel.RoutineViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoutineScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val allRoutines by viewModel.allRoutines.collectAsStateWithLifecycle()
    val todayLogs by viewModel.currentDayLogs.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    var routineToEdit by remember { mutableStateOf<RoutineItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<RoutineCategory?>(null) }

    val todayStr = remember { RoutineRepository.getTodayDateStr() }
    val currentDayOfWeek = remember { RoutineRepository.getCurrentDayOfWeek() }

    // Filter routines for today
    val filteredRoutines = allRoutines.filter { routine ->
        val matchesCategory = selectedCategoryFilter == null || routine.categoryEnum == selectedCategoryFilter
        matchesCategory
    }

    val plannedForToday = allRoutines.filter { it.isEnabled && it.isActiveOnDay(currentDayOfWeek) }
    val completedCount = todayLogs.count { it.routineId != null }
    val progressFraction = if (plannedForToday.isNotEmpty()) {
        (completedCount.toFloat() / plannedForToday.size.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Find if any routine is currently active right now
    val currentCal = Calendar.getInstance()
    val currentMinutes = currentCal.get(Calendar.HOUR_OF_DAY) * 60 + currentCal.get(Calendar.MINUTE)
    val ongoingRoutine = plannedForToday.find { item ->
        val startMins = item.startHour * 60 + item.startMinute
        var endMins = item.endHour * 60 + item.endMinute
        if (endMins <= startMins) endMins += 24 * 60 // cross midnight
        currentMinutes in startMins until endMins
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Date & Day
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "সারাদিনের সময়সূচী",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        val sdfDisplay = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
                        Text(
                            text = sdfDisplay.format(Date()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Total completion badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$completedCount/${plannedForToday.size} সম্পন্ন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Daily Progress Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "আজকের সম্পন্ন লক্ষ্য",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            // Ongoing Routine Highlight (if active now)
            if (ongoingRoutine != null) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = ongoingRoutine.categoryEnum.composeColor.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFF22C55E), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "এখন চলছে (Currently Active)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ongoingRoutine.categoryEnum.composeColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ongoingRoutine.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${ongoingRoutine.startTimeFormatted} – ${ongoingRoutine.endTimeFormatted} (${ongoingRoutine.formattedDuration})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Start Timer Button
                            IconButton(
                                onClick = { viewModel.setupTimerForRoutine(ongoingRoutine) },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(ongoingRoutine.categoryEnum.composeColor, CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "টাইমার শুরু করুন",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("সকল (${allRoutines.size})", fontSize = 12.sp) }
                    )
                    RoutineCategory.entries.forEach { cat ->
                        val count = allRoutines.count { it.categoryEnum == cat }
                        if (count > 0) {
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text("${cat.titleBn} ($count)", fontSize = 12.sp) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(cat.composeColor, CircleShape)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Routine Items List
            if (filteredRoutines.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "কোন রুটিন পাওয়া যায়নি",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "নিচের (+) বাটনে চাপ দিয়ে রুটিন যোগ করুন",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(filteredRoutines, key = { it.id }) { routine ->
                    val isCompleted = todayLogs.any { it.routineId == routine.id }
                    RoutineCard(
                        routine = routine,
                        isCompleted = isCompleted,
                        onToggleCompletion = { checked ->
                            viewModel.toggleRoutineCompletion(routine, checked)
                        },
                        onStartTimer = {
                            viewModel.setupTimerForRoutine(routine)
                        },
                        onEdit = {
                            routineToEdit = routine
                        },
                        onToggleEnabled = { enabled ->
                            viewModel.toggleRoutineEnabled(routine, enabled)
                        }
                    )
                }
            }
        }

        // Add Routine FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_routine_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "নতুন রুটিন যোগ করুন")
        }

        // Dialogs
        if (showAddDialog) {
            EditRoutineDialog(
                initialRoutine = null,
                onDismiss = { showAddDialog = false },
                onSave = { newRoutine ->
                    viewModel.saveRoutine(newRoutine)
                    showAddDialog = false
                }
            )
        }

        routineToEdit?.let { routine ->
            EditRoutineDialog(
                initialRoutine = routine,
                onDismiss = { routineToEdit = null },
                onSave = { updated ->
                    viewModel.saveRoutine(updated)
                    routineToEdit = null
                },
                onDelete = { toDelete ->
                    viewModel.deleteRoutine(toDelete)
                    routineToEdit = null
                }
            )
        }
    }
}

@Composable
fun RoutineCard(
    routine: RoutineItem,
    isCompleted: Boolean,
    onToggleCompletion: (Boolean) -> Unit,
    onStartTimer: () -> Unit,
    onEdit: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("routine_card_${routine.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Category tag, time, and complete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(routine.categoryEnum.composeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = routine.categoryEnum.titleBn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = routine.categoryEnum.composeColor
                    )
                }

                // Checkbox / Mark Done for today
                IconButton(
                    onClick = { onToggleCompletion(!isCompleted) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "সম্পন্ন হিসেবে চিহ্নিত করুন",
                        tint = if (isCompleted) Color(0xFF16A34A) else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Title & notes
            Text(
                text = routine.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )

            if (routine.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = routine.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time range & Duration Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${routine.startTimeFormatted} – ${routine.endTimeFormatted}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = routine.formattedDuration,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reminder & Sound indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val leadText = if (routine.reminderMinutesBefore == 0) "শুরুতে" else "${routine.reminderMinutesBefore} মিনিট আগে"
                    Text(
                        text = "$leadText • ${routine.soundEnum.titleEn}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Focus Timer
                    IconButton(
                        onClick = onStartTimer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = "টাইমার শুরু করুন",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Edit
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "সম্পাদনা করুন",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Active Switch
                    Switch(
                        checked = routine.isEnabled,
                        onCheckedChange = onToggleEnabled,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
