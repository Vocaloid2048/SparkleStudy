package com.voc2048.sparkle_study.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay

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
fun TaskItem(
    task: DailyTaskEntity, 
    isChallengeFinished: Boolean,
    onClaim: () -> Unit
) {
    val icon = when(task.taskType) {
        "GARDEN_WORK", "WATER" -> Icons.Default.LocalFlorist
        "BOTTLE_SEND", "BOTTLE" -> Icons.Default.Mail
        "SOCIAL_EMOJI" -> Icons.Default.EmojiEmotions
        "POMO_CYCLE" -> Icons.Default.Update
        "QUOTE_CLICK" -> Icons.Default.AutoAwesome
        else -> Icons.Default.Timer
    }
    val label = when(task.taskType) {
        "GARDEN_WORK", "WATER" -> "灌溉與施肥"
        "BOTTLE_SEND", "BOTTLE" -> "發送漂流瓶"
        "SOCIAL_EMOJI" -> "在自修室點讚"
        "POMO_CYCLE" -> "完成番茄鐘"
        "QUOTE_CLICK" -> "點擊靈感金句"
        else -> "累積專注 60 分鐘"
    }
    val unit = when(task.taskType) {
        "FOCUS_60" -> "分鐘"
        else -> "次"
    }

    Card(
        modifier = Modifier.width(180.dp).height(170.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isClaimed) MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (task.currentProgress >= task.targetValue && !task.isClaimed && !isChallengeFinished) 
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. 大圖標
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (task.isClaimed) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon, 
                        contentDescription = null, 
                        tint = if (task.isClaimed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, 
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // 2. 標題
                Text(
                    label, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = if (task.isClaimed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                
                // 3. 進度與狀態文字
                if (task.isClaimed) {
                    Text("✅ 已領取獎勵", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                } else if (isChallengeFinished) {
                    Text("每日挑戰已經完成", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                } else {
                    Text(
                        "${task.currentProgress} / ${task.targetValue} $unit", 
                        fontSize = 14.sp, 
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "獎勵: 10 🪙", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // 4. 領取按鈕 (僅在未達成 3 任務且該任務完成時顯示)
            if (task.currentProgress >= task.targetValue && !task.isClaimed && !isChallengeFinished) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clickable { onClaim() },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        "領取", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LoginRewardRow(streak: Int) {
    val rewards = listOf(5, 10, 5, 15, 5, 20, 50)
    val listState = rememberLazyListState()
    
    // Calculate current day in cycle (1-7)
    val currentDayInCycle = ((streak - 1) % 7) + 1
    
    LaunchedEffect(streak) {
        delay(500)
        // Scroll to center the current day (index is day - 1)
        val targetIndex = (currentDayInCycle - 1).coerceIn(0, 6)
        listState.animateScrollToItem(targetIndex)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("接下來 7 天登入獎勵", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(7) { index ->
                val day = index + 1
                val isClaimed = day <= currentDayInCycle
                val isToday = day == currentDayInCycle
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when {
                                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                isClaimed -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                        .padding(16.dp)
                        .width(90.dp)
                ) {
                    Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "第${day}天", 
                            fontSize = 12.sp, 
                            color = if (isClaimed) MaterialTheme.colorScheme.primary else Color.Gray, 
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isClaimed) "✅" else "🪙", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${rewards[index]}", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = if (isClaimed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
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
