package com.voc2048.sparkle_study.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voc2048.sparkle_study.database.DailyTaskEntity
import com.voc2048.sparkle_study.database.UserEntity
import com.voc2048.sparkle_study.ui.viewmodels.DashboardViewModel

@Composable
fun TopStatsBar(user: UserEntity?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 等級進度 (Level & XP)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
            CircularProgressIndicator(
                progress = { (user?.xp?.toFloat() ?: 0f) / 1000f },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(user?.level?.toString() ?: "1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 連續登入天數 (Login Streak)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.height(28.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🔥 ${user?.loginStreak ?: 1} 天", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 持有代幣數量 (Coins)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.height(28.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🪙 ${user?.coins ?: 0}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun TaskItem(task: DailyTaskEntity, onClaim: () -> Unit) {
    val icon = when(task.taskType) {
        "WATER" -> Icons.Default.LocalFlorist
        "BOTTLE" -> Icons.Default.Mail
        else -> Icons.Default.Timer
    }
    val label = when(task.taskType) {
        "WATER" -> "園丁任務：灌溉花卉"
        "BOTTLE" -> "暖心任務：發送漂流瓶"
        else -> "深度任務：專注 > 60分"
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp)
            LinearProgressIndicator(
                progress = { task.currentProgress.toFloat() / task.targetValue },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text("${task.currentProgress}/${task.targetValue}", fontSize = 12.sp, color = Color.Gray)
        
        if (task.currentProgress >= task.targetValue && !task.isClaimed) {
            Button(onClick = onClaim, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), modifier = Modifier.height(28.dp)) {
                Text("領取", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun LoginRewardRow(streak: Int) {
    val rewards = listOf(5, 10, 5, 15, 5, 20, 50)
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("接下來 7 天登入獎勵", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(7) { index ->
                val day = index + 1
                val isClaimed = day <= (streak % 7)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isClaimed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(16.dp)
                        .width(90.dp)
                ) {
                    Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                        if (isClaimed) {
                            Icon(
                                Icons.Default.Check, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Text("第${day}天", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🪙", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${rewards[index]}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (isClaimed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun FocusRangeChart(
    distribution: List<com.voc2048.sparkle_study.ui.viewmodels.FocusDistribution>,
    isWeeklyMode: Boolean,
    onToggleMode: () -> Unit
) {
    val hours = listOf("00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00", "00:00")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isWeeklyMode) "本週分布" else "最近7天分布", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = onToggleMode, modifier = Modifier.size(24.dp)) {
                    Icon(if (isWeeklyMode) Icons.Default.DateRange else Icons.Default.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            val dayLabels = distribution.map { it.label }.distinct()
            
            Row(modifier = Modifier.fillMaxWidth().padding(start = 40.dp)) {
                dayLabels.forEach { label ->
                    Text(label, modifier = Modifier.weight(1f), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
            
            Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    hours.forEach { hour ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(hour, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.width(40.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxSize().padding(start = 40.dp)) {
                    dayLabels.forEachIndexed { dayIndex, _ ->
                        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                            val dayData = distribution.filter { it.label == dayLabels[dayIndex] }
                            
                            dayData.forEach { data ->
                                if (data.focusMins > 0 || data.restMins > 0 || data.idleMins > 0) {
                                    val hourOffset = data.hour.toFloat() / 24f
                                    val color = when {
                                        data.focusMins > 0 -> MaterialTheme.colorScheme.primary
                                        data.restMins > 0 -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                    val durationMins = maxOf(data.focusMins, data.restMins, data.idleMins)
                                    val barHeightScale = (durationMins.toFloat() / 60f) * (1f / 24f)
                                    
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .fillMaxHeight(barHeightScale.coerceAtLeast(0.015f))
                                            .offset(y = 320.dp * hourOffset)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
