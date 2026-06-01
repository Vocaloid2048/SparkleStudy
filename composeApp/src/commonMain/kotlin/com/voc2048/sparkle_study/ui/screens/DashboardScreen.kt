package com.voc2048.sparkle_study.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voc2048.sparkle_study.ui.components.*
import com.voc2048.sparkle_study.ui.viewmodels.DashboardViewModel
import com.voc2048.sparkle_study.ui.viewmodels.StudyViewModel

@Composable
fun DashboardScreen(
    studyViewModel: StudyViewModel = viewModel { StudyViewModel() },
    dashboardViewModel: DashboardViewModel = viewModel { DashboardViewModel() }
) {
    val user by dashboardViewModel.user.collectAsState()
    val todayMinutes by dashboardViewModel.todayFocusMinutes.collectAsState()
    val dailyGoal by dashboardViewModel.dailyGoalMinutes.collectAsState()
    val earnings by dashboardViewModel.todayEarnings.collectAsState()
    val plant by dashboardViewModel.activePlant.collectAsState()
    val tasks by dashboardViewModel.todayTasks.collectAsState()
    val hourlyDist by dashboardViewModel.hourlyDistribution.collectAsState()
    val isWeeklyMode by dashboardViewModel.isWeeklyMode.collectAsState()
    val showTamperDialog by dashboardViewModel.showTimeTamperDialog.collectAsState()
    
    val h = todayMinutes / 60
    val m = todayMinutes % 60
    val timeDisplay = "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
    
    val goalProgress = if (dailyGoal > 0) todayMinutes.toFloat() / dailyGoal else 0f

    Scaffold(
        topBar = { TopStatsBar(user) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding())
                .fadingEdge(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 48.dp)
        ) {
            item {
                Text(
                    "今日成長",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 今日總覽卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("今日專注時間", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(timeDisplay, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("/${(dailyGoal / 60).toString().padStart(2, '0')}:${(dailyGoal % 60).toString().padStart(2, '0')}", fontSize = 18.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    StatItem("今日養分", earnings.second.toString())
                                    StatItem("今日代幣", earnings.first.toString())
                                    StatItem("持續日數", user?.loginStreak?.toString() ?: "1")
                                }
                            }

                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp).padding(start = 16.dp)) {
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color.White.copy(alpha = 0.2f),
                                    strokeWidth = 8.dp
                                )
                                // Total Plan Progress (Outer)
                                CircularProgressIndicator(
                                    progress = { (plant?.currentDay?.toFloat() ?: 1f) / (plant?.targetDays ?: 30).toFloat() },
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color.White.copy(alpha = 0.5f),
                                    strokeWidth = 8.dp
                                )
                                // Today Goal Progress (Inner)
                                CircularProgressIndicator(
                                    progress = { goalProgress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.secondary,
                                    strokeWidth = 8.dp,
                                    trackColor = Color.White
                                )
                                Text("${(goalProgress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("「今日的每一分努力，都是明日綻放的養分。」", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    }
                }
            }

            // 登入獎勵
            item {
                LoginRewardRow(user?.loginStreak ?: 1)
            }

            // 計劃卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(plant?.planName ?: "專注計劃", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(plant?.currentDay?.toString() ?: "1", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("/${plant?.targetDays ?: 30} 天", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    if (plant?.status == "DORMANT") "距離休眠還有 2 天" else "距離盛開還差 50 養分",
                                    fontSize = 12.sp, color = MaterialTheme.colorScheme.error
                                )
                            }
                            
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                                CircularProgressIndicator(
                                    progress = { (plant?.currentDay?.toFloat() ?: 1f) / (plant?.targetDays ?: 30).toFloat() },
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 6.dp
                                )
                                Icon(Icons.Default.LocalFlorist, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("保持節奏，你的植物正在茁壯成長！", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            // 每日任務
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("每日挑戰", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            val claimedCount = tasks.count { it.isClaimed }
                            Surface(
                                color = if (claimedCount >= 3) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "${claimedCount.coerceAtMost(3)} / 3 達成獎勵",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if (claimedCount >= 3) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LinearProgressIndicator(
                            progress = { (tasks.count { it.isClaimed }.toFloat() / 3f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                            gapSize = 0.dp,
                            drawStopIndicator = {}
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        val claimedCount = tasks.count { it.isClaimed }
                        val isChallengeFinished = claimedCount >= 3

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(tasks.size) { index ->
                                TaskItem(
                                    task = tasks[index], 
                                    isChallengeFinished = isChallengeFinished
                                ) { 
                                    dashboardViewModel.claimTask(tasks[index].taskId) 
                                }
                            }
                        }
                    }
                }
            }

            // 最近7天分布
            item {
                FocusRangeChart(
                    distribution = hourlyDist,
                    isWeeklyMode = isWeeklyMode,
                    onToggleMode = { dashboardViewModel.toggleChartMode() }
                )
            }
        }
    }

    if (showTamperDialog) {
        AlertDialog(
            onDismissRequest = { dashboardViewModel.dismissTimeTamperDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("偵測到時間異常", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "系統偵測到您的手機時間與實際不符。為了保持獎勵機制的公平性，請確保系統時間設定為「自動設定」，否則將無法領取每日登入獎勵。",
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { dashboardViewModel.dismissTimeTamperDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("我知道了")
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

fun Modifier.fadingEdge(): Modifier = this
    .graphicsLayer(compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                0.05f to Color.Black,
                0.95f to Color.Black,
                1f to Color.Transparent
            ),
            blendMode = BlendMode.DstIn
        )
    }

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Preview
@Composable
fun DashboardScreenPreview(){
    DashboardScreen()
}
