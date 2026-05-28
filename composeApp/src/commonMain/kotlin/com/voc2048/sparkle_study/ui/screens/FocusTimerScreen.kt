package com.voc2048.sparkle_study.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voc2048.sparkle_study.ui.components.FocusTimerProgress
import com.voc2048.sparkle_study.ui.components.FocusTimerWater
import com.voc2048.sparkle_study.utils.SparkleColorScheme
import com.voc2048.sparkle_study.utils.hazeEffectSparkle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import compose.icons.FeatherIcons
import compose.icons.feathericons.Droplet
import compose.icons.feathericons.Zap
import compose.icons.feathericons.SkipForward
import compose.icons.feathericons.Square
import com.voc2048.sparkle_study.ui.components.AppDialog
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class TimerMode {
    POMODORO, COUNT_UP, COUNT_DOWN
}

enum class TimerStyle {
    SPARKLE, WATER
}

enum class TimerSwitchBehavior {
    CONFIRM, AUTO_CONTINUE, PAUSE
}

// 模擬全局配置 (未來可移至 DataStore 或 ViewModel)
object TimerSettings {
    var switchBehavior by mutableStateOf(TimerSwitchBehavior.CONFIRM)
}

@Composable
fun FocusTimerScreen() {
    var timerMode by remember { mutableStateOf(TimerMode.POMODORO) }
    var timerStyle by remember { mutableStateOf(TimerStyle.SPARKLE) }
    
    // 番茄鐘狀態
    var pomodoroFocusMin by remember { mutableStateOf(25) }
    var pomodoroShortBreakMin by remember { mutableStateOf(5) }
    var pomodoroRounds by remember { mutableStateOf(3) }
    
    var pomodoroTimeLeft by remember { mutableStateOf(25 * 60) }
    var pomodoroTotalTime by remember { mutableStateOf(25 * 60) }
    var pomodoroIsRunning by remember { mutableStateOf(false) }
    var pomodoroStarted by remember { mutableStateOf(false) }
    var pomodoroCurrentPhase by remember { mutableStateOf(0) } 
    var pomodoroIsResting by remember { mutableStateOf(false) }

    // 正計時狀態
    var countUpTime by remember { mutableStateOf(0) }
    var countUpIsRunning by remember { mutableStateOf(false) }

    // 倒計時狀態
    var countDownTimeLeft by remember { mutableStateOf(60 * 60) }
    var countDownTotalTime by remember { mutableStateOf(60 * 60) }
    var countDownIsRunning by remember { mutableStateOf(false) }
    var countDownStarted by remember { mutableStateOf(false) }

    val hazeState = rememberHazeState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showSwitchConfirmDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var pendingMode by remember { mutableStateOf<TimerMode?>(null) }
    
    val showPomodoroSettings = remember { mutableStateOf(false) }
    val showCountDownSettings = remember { mutableStateOf(false) }

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

    // 監聽狀態更新語句
    LaunchedEffect(timerMode, pomodoroIsRunning, countUpIsRunning, countDownIsRunning, pomodoroIsResting, pomodoroStarted, countDownStarted) {
        val isRunning = when(timerMode) {
            TimerMode.POMODORO -> pomodoroIsRunning
            TimerMode.COUNT_UP -> countUpIsRunning
            TimerMode.COUNT_DOWN -> countDownIsRunning
        }
        val isStarted = when(timerMode) {
            TimerMode.POMODORO -> pomodoroStarted
            TimerMode.COUNT_UP -> countUpTime > 0
            TimerMode.COUNT_DOWN -> countDownStarted
        }
        
        val isFinished = when(timerMode) {
            TimerMode.POMODORO -> pomodoroStarted && pomodoroTimeLeft == 0 && !pomodoroIsRunning
            TimerMode.COUNT_DOWN -> countDownStarted && countDownTimeLeft == 0 && !countDownIsRunning
            else -> false
        }

        when {
            isFinished -> updateQuote("finish")
            !isStarted -> updateQuote("reset")
            isRunning -> {
                if (timerMode == TimerMode.POMODORO && pomodoroIsResting) updateQuote("resting")
                else updateQuote("running")
            }
            else -> updateQuote("paused")
        }
    }

    // 保存進度模擬
    fun saveProgress(mode: TimerMode, duration: Int) {
        println("Saving progress for $mode: $duration to DB")
    }

    // 計時邏輯
    LaunchedEffect(pomodoroIsRunning, pomodoroCurrentPhase) {
        if (pomodoroIsRunning) {
            while (pomodoroTimeLeft > 0) {
                delay(1000)
                pomodoroTimeLeft--
            }
            if (pomodoroCurrentPhase < pomodoroRounds * 2 - 1) {
                saveProgress(TimerMode.POMODORO, pomodoroTotalTime)
                pomodoroCurrentPhase++
                pomodoroIsResting = pomodoroCurrentPhase % 2 != 0
                val nextMin = if (pomodoroIsResting) pomodoroShortBreakMin else pomodoroFocusMin
                pomodoroTimeLeft = nextMin * 60
                pomodoroTotalTime = nextMin * 60
            } else {
                saveProgress(TimerMode.POMODORO, pomodoroTotalTime)
                pomodoroIsRunning = false
                // pomodoroStarted remains true to show finished state
            }
        }
    }

    LaunchedEffect(countUpIsRunning) {
        if (countUpIsRunning) {
            while (countUpIsRunning) {
                delay(1000)
                countUpTime++
            }
        }
    }

    LaunchedEffect(countDownIsRunning) {
        if (countDownIsRunning) {
            while (countDownTimeLeft > 0) {
                delay(1000)
                countDownTimeLeft--
            }
            saveProgress(TimerMode.COUNT_DOWN, countDownTotalTime)
            countDownIsRunning = false
        }
    }

    val performSwitch: (TimerMode) -> Unit = { mode ->
        saveProgress(timerMode, when(timerMode) {
            TimerMode.POMODORO -> pomodoroTotalTime - pomodoroTimeLeft
            TimerMode.COUNT_UP -> countUpTime
            TimerMode.COUNT_DOWN -> countDownTotalTime - countDownTimeLeft
        })
        
        when(TimerSettings.switchBehavior) {
            TimerSwitchBehavior.PAUSE -> {
                pomodoroIsRunning = false
                countUpIsRunning = false
                countDownIsRunning = false
                timerMode = mode
            }
            else -> {
                timerMode = mode
            }
        }
    }

    val onTabClick: (TimerMode) -> Unit = { mode ->
        if (mode != timerMode) {
            val isCurrentRunning = when (timerMode) {
                TimerMode.POMODORO -> pomodoroIsRunning
                TimerMode.COUNT_UP -> countUpIsRunning
                TimerMode.COUNT_DOWN -> countDownIsRunning
            }
            
            if (isCurrentRunning && TimerSettings.switchBehavior == TimerSwitchBehavior.CONFIRM) {
                pendingMode = mode
                showSwitchConfirmDialog = true
            } else {
                performSwitch(mode)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(SparkleColorScheme.primary.copy(alpha = 0.5f), SparkleColorScheme.background)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
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

                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffset)
                            .size(width = 85.dp, height = 40.dp)
                            .background(SparkleColorScheme.primary, RoundedCornerShape(20.dp))
                    )

                    Row {
                        tabs.forEach { mode ->
                            Box(
                                modifier = Modifier
                                    .size(width = 85.dp, height = 40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onTabClick(mode) },
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
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .hazeEffectSparkle(hazeState)
                        .background(SparkleColorScheme.background.copy(alpha = 0.6f))
                        .clickable { timerStyle = if (timerStyle == TimerStyle.SPARKLE) TimerStyle.WATER else TimerStyle.SPARKLE },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (timerStyle == TimerStyle.SPARKLE) FeatherIcons.Droplet else FeatherIcons.Zap,
                        contentDescription = null,
                        tint = SparkleColorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Text(
                text = currentQuote,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SparkleColorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // 中央計時
        Box(contentAlignment = Alignment.Center) {
            val indicatorIcon = if (timerMode == TimerMode.POMODORO && pomodoroIsResting) "🍵" else "🔥"
            val showTimerText = when(timerMode) {
                TimerMode.POMODORO -> pomodoroStarted
                TimerMode.COUNT_UP -> countUpTime > 0
                TimerMode.COUNT_DOWN -> countDownStarted
            }

            when (timerMode) {
                TimerMode.POMODORO -> {
                    val progress = if (pomodoroTotalTime > 0) pomodoroTimeLeft.toFloat() / pomodoroTotalTime else 1f
                    if (timerStyle == TimerStyle.WATER) {
                        FocusTimerWater(displayTime = pomodoroTimeLeft, progress = progress, isRunning = pomodoroIsRunning, hazeState = hazeState, isTimerWaving = true, showText = showTimerText)
                    } else {
                        FocusTimerProgress(displayTime = pomodoroTimeLeft, progress = progress, isRunning = pomodoroIsRunning, hazeState = hazeState, isReverse = false, indicatorIcon = indicatorIcon, showText = showTimerText)
                    }
                }
                TimerMode.COUNT_UP -> {
                    if (timerStyle == TimerStyle.WATER) {
                        FocusTimerWater(displayTime = countUpTime, progress = 0.8f, isRunning = countUpIsRunning, hazeState = hazeState, isTimerWaving = false, showText = true)
                    } else {
                        FocusTimerProgress(displayTime = countUpTime, progress = 1f - (countUpTime % 3600) / 3600f, isRunning = countUpIsRunning, hazeState = hazeState, isReverse = false, indicatorIcon = indicatorIcon, showText = true)
                    }
                }
                TimerMode.COUNT_DOWN -> {
                    val progress = if (countDownTotalTime > 0) countDownTimeLeft.toFloat() / countDownTotalTime else 1f
                    if (timerStyle == TimerStyle.WATER) {
                        FocusTimerWater(displayTime = countDownTimeLeft, progress = progress, isRunning = countDownIsRunning, hazeState = hazeState, isTimerWaving = true, showText = showTimerText)
                    } else {
                        FocusTimerProgress(displayTime = countDownTimeLeft, progress = progress, isRunning = countDownIsRunning, hazeState = hazeState, isReverse = false, indicatorIcon = indicatorIcon, showText = showTimerText)
                    }
                }
            }

            // 設定文字 Box
            if (!showTimerText && timerMode != TimerMode.COUNT_UP) {
                val settingsStr = if (timerMode == TimerMode.POMODORO) "$pomodoroFocusMin/$pomodoroShortBreakMin/$pomodoroRounds"
                                 else {
                                     val h = countDownTotalTime / 3600
                                     val m = (countDownTotalTime % 3600) / 60
                                     val s = countDownTotalTime % 60
                                     "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
                                 }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.6f))
                        .clickable {
                            if (timerMode == TimerMode.POMODORO) showPomodoroSettings.value = true
                            else showCountDownSettings.value = true
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = settingsStr, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SparkleColorScheme.primary)
                }
            }
        }

        // 底部按鈕
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 重置
                ActionButton(
                    icon = FeatherIcons.Square,
                    tint = SparkleColorScheme.error,
                    hazeState = hazeState,
                    onClick = { showResetConfirmDialog = true }
                )
                
                // 開始/暫停
                val isRunning = when(timerMode) {
                    TimerMode.POMODORO -> pomodoroIsRunning
                    TimerMode.COUNT_UP -> countUpIsRunning
                    TimerMode.COUNT_DOWN -> countDownIsRunning
                }
                ActionButton(
                    icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    tint = SparkleColorScheme.primary,
                    hazeState = hazeState,
                    isLarge = true,
                    onClick = {
                        when (timerMode) {
                            TimerMode.POMODORO -> { pomodoroIsRunning = !pomodoroIsRunning; pomodoroStarted = true }
                            TimerMode.COUNT_UP -> { countUpIsRunning = !countUpIsRunning }
                            TimerMode.COUNT_DOWN -> { countDownIsRunning = !countDownIsRunning; countDownStarted = true }
                        }
                    }
                )
                
                // 跳過
                ActionButton(
                    icon = FeatherIcons.SkipForward,
                    tint = if (timerMode == TimerMode.POMODORO) SparkleColorScheme.secondary else Color.Gray.copy(alpha = 0.5f),
                    hazeState = hazeState,
                    onClick = {
                        if (timerMode == TimerMode.POMODORO) {
                            if (pomodoroCurrentPhase < pomodoroRounds * 2 - 1) {
                                saveProgress(TimerMode.POMODORO, pomodoroTotalTime - pomodoroTimeLeft)
                                pomodoroCurrentPhase++
                                pomodoroIsResting = pomodoroCurrentPhase % 2 != 0
                                val nextMin = if (pomodoroIsResting) pomodoroShortBreakMin else pomodoroFocusMin
                                pomodoroTimeLeft = nextMin * 60
                                pomodoroTotalTime = nextMin * 60
                                pomodoroIsRunning = true
                                pomodoroStarted = true
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("本計時器不支援跳過功能")
                            }
                        }
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 140.dp)
        )
    }

    // 彈窗邏輯
    if (showSwitchConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchConfirmDialog = false },
            title = { Text("暫停計時？") },
            text = { Text("切換模式將會暫停當前的計時並記錄進度。") },
            confirmButton = {
                TextButton(onClick = {
                    pendingMode?.let { performSwitch(it) }
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
            text = { Text("這將會結束當前計時並記錄已專注的時間。") },
            confirmButton = {
                TextButton(onClick = {
                    saveProgress(timerMode, when(timerMode) {
                        TimerMode.POMODORO -> pomodoroTotalTime - pomodoroTimeLeft
                        TimerMode.COUNT_UP -> countUpTime
                        TimerMode.COUNT_DOWN -> countDownTotalTime - countDownTimeLeft
                    })
                    when (timerMode) {
                        TimerMode.POMODORO -> { 
                            pomodoroIsRunning = false 
                            pomodoroTimeLeft = pomodoroFocusMin * 60 
                            pomodoroTotalTime = pomodoroFocusMin * 60 
                            pomodoroStarted = false 
                            pomodoroCurrentPhase = 0 
                            pomodoroIsResting = false 
                        }
                        TimerMode.COUNT_UP -> { 
                            countUpIsRunning = false 
                            countUpTime = 0 
                        }
                        TimerMode.COUNT_DOWN -> { 
                            countDownIsRunning = false 
                            countDownTimeLeft = countDownTotalTime 
                            countDownStarted = false 
                        }
                    }
                    showResetConfirmDialog = false
                }) { Text("確認") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("取消") }
            }
        )
    }

    AppDialog("設定番茄時間", hazeState, showPomodoroSettings) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NumberPickerSlot("專注", pomodoroFocusMin) { pomodoroFocusMin = it; if(!pomodoroStarted) { pomodoroTimeLeft = it * 60; pomodoroTotalTime = it * 60 } }
            NumberPickerSlot("短休", pomodoroShortBreakMin) { pomodoroShortBreakMin = it }
            NumberPickerSlot("輪次", pomodoroRounds, 1..10) { pomodoroRounds = it }
        }
    }

    AppDialog("設定倒計時", hazeState, showCountDownSettings) {
        var h by remember { mutableStateOf(countDownTotalTime / 3600) }
        var m by remember { mutableStateOf((countDownTotalTime % 3600) / 60) }
        var s by remember { mutableStateOf(countDownTotalTime % 60) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NumberPickerSlot("時", h, 0..23) { h = it }
            NumberPickerSlot("分", m, 0..59) { m = it }
            NumberPickerSlot("秒", s, 0..59) { s = it }
        }
        LaunchedEffect(h, m, s) {
            val total = h * 3600 + m * 60 + s
            countDownTotalTime = total
            if(!countDownStarted) countDownTimeLeft = total
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, tint: Color, hazeState: HazeState, isLarge: Boolean = false, onClick: () -> Unit) {
    val size = 64.dp // 統一大小
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, tint.copy(alpha = 0.5f), CircleShape)
            .hazeEffectSparkle(hazeState)
            .background(SparkleColorScheme.background.copy(alpha = 0.4f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun NumberPickerSlot(label: String, value: Int, range: IntRange = 1..120, onValueChange: (Int) -> Unit) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = (value - range.first))
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
        Text(label, fontSize = 12.sp, color = SparkleColorScheme.primary)
        Box(modifier = Modifier.height(100.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            LazyColumn(state = state, modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                items(range.last - range.first + 1) { index ->
                    val num = range.first + index
                    val isSelected = num == value
                    Text(
                        text = num.toString(),
                        fontSize = if (isSelected) 20.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SparkleColorScheme.primary else Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp).clickable { onValueChange(num) }
                    )
                }
            }
            LaunchedEffect(state.isScrollInProgress) {
                if (!state.isScrollInProgress) {
                    onValueChange(range.first + state.firstVisibleItemIndex)
                }
            }
        }
    }
}
