package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RoutineCategory
import com.example.ui.viewmodel.RoutineViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val allRoutines by viewModel.allRoutines.collectAsStateWithLifecycle()

    var timerModeIndex by remember { mutableIntStateOf(if (timerState.isStopwatch) 1 else 0) }

    fun formatSeconds(sec: Int): String {
        val hrs = sec / 3600
        val mins = (sec % 3600) / 60
        val secs = sec % 60
        return if (hrs > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        }
    }

    val displayTime = if (timerState.isStopwatch) {
        formatSeconds(timerState.elapsedSeconds)
    } else {
        formatSeconds(timerState.remainingSeconds)
    }

    val progressFraction = if (timerState.isStopwatch) {
        1f
    } else {
        if (timerState.totalSeconds > 0) {
            (timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "timer_progress")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "ফোকাস টাইমার ও ট্র্যাকার",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "পড়া বা কাজ শুরু করুন এবং ব্যয়িত সময় স্বয়ংক্রিয়ভাবে রেকর্ড করুন",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Mode Tab: Countdown vs Stopwatch
        item {
            TabRow(
                selectedTabIndex = timerModeIndex,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = timerModeIndex == 0,
                    onClick = {
                        timerModeIndex = 0
                        viewModel.setupCustomTimer(
                            title = timerState.title,
                            category = timerState.category,
                            minutes = 25,
                            isStopwatch = false
                        )
                    },
                    text = { Text("কাউন্টডাউন (Timer)", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = timerModeIndex == 1,
                    onClick = {
                        timerModeIndex = 1
                        viewModel.setupCustomTimer(
                            title = timerState.title,
                            category = timerState.category,
                            minutes = 0,
                            isStopwatch = true
                        )
                    },
                    text = { Text("স্টপওয়াচ (Stopwatch)", fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        // Activity selector chips (from existing routines)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "কোন কাজের জন্য টাইমার? (Select Activity):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allRoutines.forEach { routine ->
                        val isSelected = timerState.routineId == routine.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setupTimerForRoutine(routine)
                            },
                            label = { Text(routine.title, fontSize = 12.sp) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(routine.categoryEnum.composeColor, CircleShape)
                                )
                            }
                        )
                    }

                    // Quick generic category chips if needed
                    RoutineCategory.entries.forEach { cat ->
                        if (allRoutines.none { it.categoryEnum == cat }) {
                            FilterChip(
                                selected = timerState.category == cat && timerState.routineId == null,
                                onClick = {
                                    viewModel.setupCustomTimer(
                                        title = cat.titleBn,
                                        category = cat,
                                        minutes = if (timerModeIndex == 0) 30 else 0,
                                        isStopwatch = timerModeIndex == 1
                                    )
                                },
                                label = { Text(cat.titleBn, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Quick Presets (for countdown mode)
        if (timerModeIndex == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        15 to "১৫ মি.",
                        25 to "পোমোডোরো (২৫ মি.)",
                        45 to "৪৫ মি.",
                        60 to "৬০ মি.",
                        90 to "৯০ মি."
                    ).forEach { (mins, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (timerState.totalSeconds == mins * 60) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.clickable {
                                viewModel.setupCustomTimer(
                                    title = timerState.title,
                                    category = timerState.category,
                                    minutes = mins,
                                    isStopwatch = false
                                )
                            }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                color = if (timerState.totalSeconds == mins * 60) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }

        // Circular Timer Display
        item {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = timerState.category.composeColor
                val trackColor = MaterialTheme.colorScheme.surfaceVariant

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background track
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Progress arc
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = timerState.category.composeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = timerState.category.titleBn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = timerState.category.composeColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = displayTime,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = timerState.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Control Buttons
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                OutlinedButton(
                    onClick = { viewModel.stopTimer() },
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "রিসেট করুন")
                }

                // Play / Pause Main Button
                Button(
                    onClick = {
                        if (timerState.isRunning) {
                            viewModel.pauseTimer()
                        } else {
                            viewModel.startTimer()
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("timer_play_pause_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = timerState.category.composeColor
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timerState.isRunning) "বিরতি" else "শুরু",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Finish & Log Hours Button
                Button(
                    onClick = { viewModel.finishAndLogTimer() },
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "সম্পন্ন ও রেকর্ড", tint = Color.White)
                }
            }
        }

        // Action Label
        item {
            Text(
                text = if (timerState.isRunning) "টাইমার চলছে... মনোযোগ বজায় রাখুন" else "শুরু করতে প্লে বাটনে চাপ দিন",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Study Tip Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 পড়াশোনার টিপস:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "প্রতি ২৫ মিনিট পড়ার পর ৫ মিনিটের বিরতি নিন। প্রাইভেট বা কলেজের লেকচারগুলো সন্ধ্যার পড়ার সময় রিভিশন দিলে পড়া দীর্ঘস্থায়ী হয়।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
