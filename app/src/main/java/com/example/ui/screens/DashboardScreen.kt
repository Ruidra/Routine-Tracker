package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RoutineCategory
import com.example.data.repository.RoutineRepository
import com.example.ui.viewmodel.RoutineViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val allRoutines by viewModel.allRoutines.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val sleepLogs by viewModel.sleepLogs.collectAsStateWithLifecycle()
    val todayDateStr = remember { RoutineRepository.getTodayDateStr() }
    val currentDayOfWeek = remember { RoutineRepository.getCurrentDayOfWeek() }

    // Today's hours for each required category
    val selfStudyHours = remember(allLogs, todayDateStr) {
        viewModel.getCategoryHoursForDate(RoutineCategory.SELF_STUDY, allLogs, sleepLogs, todayDateStr)
    }
    val collegeHours = remember(allLogs, todayDateStr) {
        viewModel.getCategoryHoursForDate(RoutineCategory.COLLEGE, allLogs, sleepLogs, todayDateStr)
    }
    val privateHours = remember(allLogs, todayDateStr) {
        viewModel.getCategoryHoursForDate(RoutineCategory.PRIVATE_TUITION, allLogs, sleepLogs, todayDateStr)
    }
    val restHours = remember(allLogs, todayDateStr) {
        viewModel.getCategoryHoursForDate(RoutineCategory.REST, allLogs, sleepLogs, todayDateStr)
    }
    val sleepHours = remember(sleepLogs, todayDateStr) {
        viewModel.getCategoryHoursForDate(RoutineCategory.SLEEP, allLogs, sleepLogs, todayDateStr)
    }

    val totalStudyAndAcademicHours = selfStudyHours + collegeHours + privateHours

    // Completion Rate
    val plannedToday = allRoutines.filter { it.isEnabled && it.isActiveOnDay(currentDayOfWeek) }
    val completedToday = allLogs.filter { it.dateStr == todayDateStr }.distinctBy { it.routineId ?: it.id }
    val completionPercentage = if (plannedToday.isNotEmpty()) {
        ((completedToday.size.toFloat() / plannedToday.size.toFloat()) * 100).toInt().coerceAtMost(100)
    } else if (completedToday.isNotEmpty()) 100 else 0

    // Past 7 Days trend
    val weekStats = remember(allRoutines, allLogs, sleepLogs) {
        viewModel.getWeekDayStats(allRoutines, allLogs, sleepLogs)
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
                    text = "অগ্রগতি ড্যাশবোর্ড",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "সারাদিনের সময় ব্যয় ও রুটিন সম্পন্ন করার সম্পূর্ণ বিশ্লেষণ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Completion Rate Hero Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "আজকের রুটিন সম্পন্নতার হার",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "$completionPercentage%",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "${completedToday.size}টি সম্পন্ন / মোট ${plannedToday.size}টি কাজ নির্ধারিত",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    // Circular Progress
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { completionPercentage / 100f },
                            modifier = Modifier.size(76.dp),
                            strokeWidth = 8.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "$completionPercentage%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Total Academic Productive Hours Highlight
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF065F46) // Deep emerald green
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "মোট পড়াশোনার সময় (Study & Academic Time)",
                            fontSize = 12.sp,
                            color = Color(0xFFA7F3D0)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "প্রাইভেট + কলেজ + নিজের পড়ার যোগফল",
                            fontSize = 11.sp,
                            color = Color(0xFFD1FAE5)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF047857)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f ঘণ্টা", totalStudyAndAcademicHours),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Section Title: Category-by-Category Hours Breakdown
        item {
            Text(
                text = "বিষয়ভিত্তিক সময় ব্যয়ের তালিকা (Hours Breakdown):",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // 1. Self Study Hours Card
        item {
            CategoryHourMetricCard(
                titleBn = "নিজের পড়ার সময় (Self-Study)",
                hours = selfStudyHours,
                icon = Icons.Default.MenuBook,
                color = Color(0xFF059669),
                targetHours = 4.0f,
                description = "বই পড়া, রিভিশন ও প্র্যাকটিস"
            )
        }

        // 2. College Hours Card
        item {
            CategoryHourMetricCard(
                titleBn = "কলেজের সময় (College & Classes)",
                hours = collegeHours,
                icon = Icons.Default.AccountBalance,
                color = Color(0xFF2563EB),
                targetHours = 4.0f,
                description = "ক্লাস লেকচার ও ল্যাবরেটরি"
            )
        }

        // 3. Private Tuition Hours Card
        item {
            CategoryHourMetricCard(
                titleBn = "প্রাইভেট পড়ার সময় (Private Tuition)",
                hours = privateHours,
                icon = Icons.Default.School,
                color = Color(0xFF7C3AED),
                targetHours = 3.0f,
                description = "কোচিং ও গৃহশিক্ষকের সময়"
            )
        }

        // 4. Sleep Hours Card
        item {
            CategoryHourMetricCard(
                titleBn = "ঘুমের সময় (Sleep Hours)",
                hours = sleepHours,
                icon = Icons.Default.Bedtime,
                color = Color(0xFF4338CA),
                targetHours = 8.0f,
                description = "রাতের ঘুম ও দুপুরের বিশ্রাম"
            )
        }

        // 5. Rest / Leisure Hours Card
        item {
            CategoryHourMetricCard(
                titleBn = "বিশ্রামের সময় (Rest & Leisure)",
                hours = restHours,
                icon = Icons.Default.Coffee,
                color = Color(0xFFEA580C),
                targetHours = 2.0f,
                description = "খাবার, নামাজ ও অবসর সময়"
            )
        }

        // Past 7 Days Weekly Comparison Chart
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
                            text = "গত ৭ দিনের অগ্রগতি (Weekly Trends)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "সম্পন্নতার হার",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7 Bar Columns
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weekStats.forEach { dayStat ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${dayStat.completionPercent}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // Bar
                                val barHeightFraction = (dayStat.completionPercent / 100f).coerceIn(0.08f, 1f)
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height((80 * barHeightFraction).dp)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            if (dayStat.completionPercent >= 70) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant
                                        )
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dayStat.dayLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHourMetricCard(
    titleBn: String,
    hours: Float,
    icon: ImageVector,
    color: Color,
    targetHours: Float,
    description: String
) {
    val progress = (hours / targetHours).coerceIn(0f, 1f)
    val hrsInt = hours.toInt()
    val minsInt = ((hours - hrsInt) * 60).toInt()
    val durationFormatted = when {
        hrsInt > 0 && minsInt > 0 -> "${hrsInt} ঘণ্টা ${minsInt} মিনিট"
        hrsInt > 0 -> "${hrsInt} ঘণ্টা"
        minsInt > 0 -> "${minsInt} মিনিট"
        else -> "০ ঘণ্টা (এখনো শুরু হয়নি)"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = titleBn,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Time spent tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f h", hours),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Duration text & target comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = durationFormatted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (hours > 0) color else MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "লক্ষ্য: ${targetHours.toInt()} ঘণ্টা",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
