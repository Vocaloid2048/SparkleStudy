package com.voc2048.sparkle_study.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voc2048.sparkle_study.ui.components.FocusTimerProgress
import com.voc2048.sparkle_study.ui.components.FocusTimerWater
import com.voc2048.sparkle_study.ui.components.NumberPickerSlot
import com.voc2048.sparkle_study.ui.viewmodels.StudyViewModel
import com.voc2048.sparkle_study.ui.viewmodels.TimerMode
import com.voc2048.sparkle_study.ui.viewmodels.TimerSwitchBehavior
import com.voc2048.sparkle_study.utils.Preferences
import com.voc2048.sparkle_study.utils.SparkleColorScheme
import com.voc2048.sparkle_study.utils.hazeEffectSparkle
import compose.icons.FeatherIcons
import compose.icons.feathericons.Droplet
import compose.icons.feathericons.SkipForward
import compose.icons.feathericons.Square
import compose.icons.feathericons.Zap
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import files.Res
import files.tomato
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

enum class TimerStyle {
    SPARKLE, WATER
}

// 模擬全局配置 (未來可移至 DataStore 或 ViewModel)
object TimerSettings {
    var switchBehavior by mutableStateOf(TimerSwitchBehavior.CONFIRM)
}

@Composable
fun FocusTimerScreen(viewModel: StudyViewModel = viewModel { StudyViewModel() }) {
    val prefs = remember { Preferences() }

    val timerMode by viewModel.timerMode.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val isResting by viewModel.isResting.collectAsState()
    val pomodoroPhase by viewModel.pomodoroPhase.collectAsState()

    var timerStyle by remember { mutableStateOf(TimerStyle.valueOf(prefs.timerStyle)) }
    
    // UI Local Config (only for the pickers)
    var pomodoroFocusMin by remember { mutableStateOf(prefs.pomodoroFocusMin) }
    var pomodoroShortBreakMin by remember { mutableStateOf(prefs.pomodoroBreakMin) }
    var pomodoroRounds by remember { mutableStateOf(prefs.pomodoroRounds) }
    var countDownTotalSeconds by remember { mutableStateOf(prefs.countdownTotalSeconds) }

    // Logic to determine if a session has "started" (i.e., we are not in initial state)
    val isStarted = when(timerMode) {
        TimerMode.POMODORO -> pomodoroPhase > 0 || timeLeft < pomodoroFocusMin * 60
        TimerMode.COUNT_UP -> timeLeft > 0
        TimerMode.COUNT_DOWN -> timeLeft < countDownTotalSeconds
    }

    // Init Global Settings
    LaunchedEffect(Unit) {
        TimerSettings.switchBehavior = TimerSwitchBehavior.valueOf(prefs.timerSwitchBehavior)
    }

    // Save style preference
    LaunchedEffect(timerStyle) {
        prefs.timerStyle = timerStyle.name
    }

    val hazeState = rememberHazeState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showSwitchConfirmDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var pendingMode by remember { mutableStateOf<TimerMode?>(null) }

    val quotes = remember {
        mapOf(
            "running" to listOf("專注當下，星光不負趕路人。", "你的每一分努力，都在點亮未來。", "心無旁騖，享受這段純粹的時光。", "持之以恆，目標就在前方。", "專注的人，本身就在發光。"),
            "paused" to listOf("深呼吸，給心靈一個緩衝。", "休息是為了走更遠的路。", "暫停片刻，整理心情再出發。", "享受這一刻的寧靜。"),
            "resting" to listOf("辛苦了！好好享受這段放鬆時光。", "補充能量，為了下一次的衝刺。", "讓大腦休息一下，靈感正在路上。", "現在是充電時間，請盡情放鬆。"),
            "reset" to listOf("全新的開始，準備好閃耀了嗎？", "歸零，是為了更好的出發。", "定下目標，開啟專注之旅。", "準備好迎接挑戰了嗎？"),
            "finish" to listOf("太棒了！你的努力已經化作星光。", "辛苦了，這段專注時光讓你更強大。", "專注達成！給自己一個大大的肯定。", "恭喜完成！每一秒的堅持都值得感概。")
        )
    }
    
    var currentQuote by remember { mutableStateOf("") }

    fun updateQuote(state: String) {
        val list = quotes[state] ?: return
        currentQuote = list[Random.nextInt(list.size)]
    }

    // Update Quotes
    LaunchedEffect(timerMode, isRunning, isResting, isStarted) {
        val isFinished = (timerMode != TimerMode.COUNT_UP) && timeLeft == 0 && !isRunning && isStarted
        when {
            isFinished -> updateQuote("finish")
            !isStarted -> updateQuote("reset")
            isRunning -> if (timerMode == TimerMode.POMODORO && isResting) updateQuote("resting") else updateQuote("running")
            else -> updateQuote("paused")
        }
    }

    val onTabClick: (TimerMode) -> Unit = { mode ->
        if (mode != timerMode) {
            if (isRunning && TimerSettings.switchBehavior == TimerSwitchBehavior.CONFIRM) {
                pendingMode = mode
                showSwitchConfirmDialog = true
            } else {
                viewModel.switchMode(mode, TimerSettings.switchBehavior)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().hazeSource(state = hazeState),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(SparkleColorScheme.primary.copy(alpha = 0.5f), SparkleColorScheme.background))))

        Column(
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .hazeEffectSparkle(hazeState)
                        .background(SparkleColorScheme.background.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(4.dp)
                ) {
                    val tabs = listOf(TimerMode.POMODORO, TimerMode.COUNT_UP, TimerMode.COUNT_DOWN)
                    val indicatorOffset by animateDpAsState(
                        targetValue = when (timerMode) {
                            TimerMode.POMODORO -> 0.dp
                            TimerMode.COUNT_UP -> 85.dp
                            TimerMode.COUNT_DOWN -> 170.dp
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy)
                    )

                    Box(modifier = Modifier.offset(x = indicatorOffset).size(width = 85.dp, height = 40.dp).background(SparkleColorScheme.primary, RoundedCornerShape(20.dp)))

                    Row {
                        tabs.forEach { mode ->
                            Box(
                                modifier = Modifier.size(width = 85.dp, height = 40.dp).clip(RoundedCornerShape(20.dp)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onTabClick(mode) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(mode) { TimerMode.POMODORO -> "番茄鐘"; TimerMode.COUNT_UP -> "正計時"; else -> "倒計時" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (timerMode == mode) SparkleColorScheme.onPrimary else SparkleColorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).hazeEffectSparkle(hazeState).background(SparkleColorScheme.background.copy(alpha = 0.6f)).clickable { timerStyle = if (timerStyle == TimerStyle.SPARKLE) TimerStyle.WATER else TimerStyle.SPARKLE },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = if (timerStyle == TimerStyle.SPARKLE) FeatherIcons.Droplet else FeatherIcons.Zap, contentDescription = null, tint = SparkleColorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            
            Text(text = currentQuote, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = SparkleColorScheme.onBackground.copy(alpha = 0.8f), modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }

        // Timer Display
        Box(contentAlignment = Alignment.Center) {
            val indicatorIcon = if (timerMode == TimerMode.POMODORO && isResting) "🍵" else "🔥"
            val showTimerText = isStarted || isRunning

            val totalTimeForProgress = when(timerMode) {
                TimerMode.POMODORO -> if (isResting) pomodoroShortBreakMin * 60 else pomodoroFocusMin * 60
                TimerMode.COUNT_DOWN -> countDownTotalSeconds
                else -> 3600
            }
            val progress = if (totalTimeForProgress > 0) {
                if (timerMode == TimerMode.COUNT_UP) (timeLeft % 3600).toFloat() / 3600f
                else timeLeft.toFloat() / totalTimeForProgress
            } else 1f

            if (timerStyle == TimerStyle.WATER) {
                FocusTimerWater(displayTime = timeLeft, progress = progress, isRunning = isRunning, hazeState = hazeState, isTimerWaving = timerMode != TimerMode.COUNT_UP, showText = showTimerText, isTimerTextBack = true, showTomato = timerMode == TimerMode.POMODORO)
            } else {
                FocusTimerProgress(displayTime = timeLeft, progress = progress, isRunning = isRunning, hazeState = hazeState, isReverse = false, indicatorIcon = indicatorIcon, showText = showTimerText, showTomato = timerMode == TimerMode.POMODORO)
            }

            if (!showTimerText && timerMode != TimerMode.COUNT_UP) {
                Box(modifier = Modifier.size(320.dp), contentAlignment = Alignment.Center) {
                    if (timerMode == TimerMode.POMODORO) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(painter = painterResource(Res.drawable.tomato), contentDescription = null, modifier = Modifier.size(260.dp).alpha(0.8f))
                            Box(modifier = Modifier.size(260.dp).background(Color.White.copy(alpha = 0.2f), CircleShape))
                        }
                    }

                    Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                            if (timerMode == TimerMode.POMODORO) {
                                NumberPickerSlot(value = pomodoroFocusMin, range = 1..120, unit = "分", modifier = Modifier.weight(1f)) { 
                                    pomodoroFocusMin = it
                                    viewModel.updatePomodoroSettings(it, pomodoroShortBreakMin, pomodoroRounds)
                                }
                                NumberPickerSlot(value = pomodoroShortBreakMin, range = 1..30, unit = "休", modifier = Modifier.weight(1f)) { 
                                    pomodoroShortBreakMin = it 
                                    viewModel.updatePomodoroSettings(pomodoroFocusMin, it, pomodoroRounds)
                                }
                                NumberPickerSlot(value = pomodoroRounds, range = 1..10, unit = "輪", modifier = Modifier.weight(1f)) { 
                                    pomodoroRounds = it 
                                    viewModel.updatePomodoroSettings(pomodoroFocusMin, pomodoroShortBreakMin, it)
                                }
                            } else if (timerMode == TimerMode.COUNT_DOWN) {
                                val h = countDownTotalSeconds / 3600
                                val m = (countDownTotalSeconds % 3600) / 60
                                val s = countDownTotalSeconds % 60
                                
                                NumberPickerSlot(value = h, range = 0..23, unit = "時", modifier = Modifier.weight(1f)) { newH ->
                                    countDownTotalSeconds = newH * 3600 + m * 60 + s
                                    viewModel.updateCountDownSettings(countDownTotalSeconds)
                                }
                                NumberPickerSlot(value = m, range = 0..59, unit = "分", modifier = Modifier.weight(1f)) { newM ->
                                    countDownTotalSeconds = h * 3600 + newM * 60 + s
                                    viewModel.updateCountDownSettings(countDownTotalSeconds)
                                }
                                NumberPickerSlot(value = s, range = 0..59, unit = "秒", modifier = Modifier.weight(1f)) { newS ->
                                    countDownTotalSeconds = h * 3600 + m * 60 + newS
                                    viewModel.updateCountDownSettings(countDownTotalSeconds)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Buttons
        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 60.dp), contentAlignment = Alignment.Center) {
            if (!isRunning && !isStarted && timerMode != TimerMode.COUNT_UP) {
                Button(
                    onClick = { viewModel.startTimer() },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.9f), contentColor = SparkleColorScheme.primary),
                    modifier = Modifier.fillMaxWidth(0.5f).height(48.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (timerMode == TimerMode.POMODORO) "開始專注" else "開始計時", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { showResetConfirmDialog = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = SparkleColorScheme.error),
                        modifier = Modifier.weight(1f).height(48.dp),
                        border = BorderStroke(1.dp, SparkleColorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(FeatherIcons.Square, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("重置", fontSize = 14.sp)
                    }
                    
                    Button(
                        onClick = { if (isRunning) viewModel.pauseTimer() else viewModel.startTimer() },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.9f), contentColor = SparkleColorScheme.primary),
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isRunning) "暫停" else if (isStarted) "繼續" else "開始", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { viewModel.skipPhase() },
                        shape = RoundedCornerShape(24.dp),
                        enabled = timerMode == TimerMode.POMODORO,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = SparkleColorScheme.secondary, disabledContainerColor = Color.White.copy(alpha = 0.05f), disabledContentColor = Color.Gray),
                        modifier = Modifier.weight(1f).height(48.dp),
                        border = BorderStroke(1.dp, SparkleColorScheme.secondary.copy(alpha = 0.5f))
                    ) {
                        Icon(FeatherIcons.SkipForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("跳過", fontSize = 14.sp)
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 140.dp))
    }

    if (showSwitchConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchConfirmDialog = false },
            title = { Text("暫停計時？") },
            text = { Text("切換模式將會暫停當前的計時並記錄進度。") },
            confirmButton = {
                TextButton(onClick = {
                    pendingMode?.let { viewModel.switchMode(it, TimerSettings.switchBehavior) }
                    showSwitchConfirmDialog = false
                }) { Text("確認") }
            },
            dismissButton = {
                TextButton(onClick = { showSwitchConfirmDialog = false }) { Text("取消") }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("重置計時？") },
            text = { Text("這將會結束當前計時並還原時間。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.stopTimer()
                    showResetConfirmDialog = false
                }) { Text("確認") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
@Preview
fun FocusTimerScreenPreview(){
    FocusTimerScreen()
}
