package com.voc2048.sparkle_study.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import org.jetbrains.compose.resources.painterResource
import files.Res
import com.voc2048.sparkle_study.ui.components.ActionButton
import com.voc2048.sparkle_study.ui.components.FocusTimerProgress
import com.voc2048.sparkle_study.ui.components.FocusTimerWater
import com.voc2048.sparkle_study.ui.components.NumberPickerSlot
import com.voc2048.sparkle_study.utils.SparkleColorScheme
import com.voc2048.sparkle_study.utils.hazeEffectSparkle
import compose.icons.FeatherIcons
import compose.icons.feathericons.Droplet
import compose.icons.feathericons.SkipForward
import compose.icons.feathericons.Square
import compose.icons.feathericons.Zap
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import files.tomato
import kotlinx.coroutines.delay
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
                        FocusTimerWater(displayTime = pomodoroTimeLeft, progress = progress, isRunning = pomodoroIsRunning, hazeState = hazeState, isTimerWaving = true, showText = showTimerText, isTimerTextBack = true, showTomato = true)
                    } else {
                        FocusTimerProgress(displayTime = pomodoroTimeLeft, progress = progress, isRunning = pomodoroIsRunning, hazeState = hazeState, isReverse = false, indicatorIcon = indicatorIcon, showText = showTimerText, showTomato = true)
                    }
                }
                TimerMode.COUNT_UP -> {
                    if (timerStyle == TimerStyle.WATER) {
                        FocusTimerWater(displayTime = countUpTime, progress = 0.8f, isRunning = countUpIsRunning, hazeState = hazeState, isTimerWaving = false, showText = true, isTimerTextBack = true)
                    } else {
                        FocusTimerProgress(displayTime = countUpTime, progress = 1f - (countUpTime % 3600) / 3600f, isRunning = countUpIsRunning, hazeState = hazeState, isReverse = false, indicatorIcon = indicatorIcon, showText = true)
                    }
                }
                TimerMode.COUNT_DOWN -> {
                    val progress = if (countDownTotalTime > 0) countDownTimeLeft.toFloat() / countDownTotalTime else 1f
                    if (timerStyle == TimerStyle.WATER) {
                        FocusTimerWater(displayTime = countDownTimeLeft, progress = progress, isRunning = countDownIsRunning, hazeState = hazeState, isTimerWaving = true, showText = showTimerText, isTimerTextBack = true)
                    } else {
                        FocusTimerProgress(displayTime = countDownTimeLeft, progress = progress, isRunning = countDownIsRunning, hazeState = hazeState, isReverse = false, indicatorIcon = indicatorIcon, showText = showTimerText)
                    }
                }
            }

            // 整合式時間選擇器 (取代原有的 Dialog)
            if (!showTimerText && timerMode != TimerMode.COUNT_UP) {
                Box(
                    modifier = Modifier.size(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (timerMode == TimerMode.POMODORO) {
                        // 蕃茄背景 (與計時中大小一致 260.dp, alpha 0.8)
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(Res.drawable.tomato),
                                contentDescription = null,
                                modifier = Modifier.size(260.dp).alpha(0.8f)
                            )
                            // 淺白色前景 overlay (統一使用開始後的效果)
                            Box(
                                modifier = Modifier
                                    .size(260.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.size(260.dp), // 與背景一致
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        ) {
                            if (timerMode == TimerMode.POMODORO) {
                                NumberPickerSlot(value = pomodoroFocusMin, range = 1..120, unit = "分", modifier = Modifier.weight(1f)) { 
                                    pomodoroFocusMin = it
                                    pomodoroTimeLeft = it * 60
                                    pomodoroTotalTime = it * 60
                                }
                                NumberPickerSlot(value = pomodoroShortBreakMin, range = 1..30, unit = "休", modifier = Modifier.weight(1f)) { 
                                    pomodoroShortBreakMin = it 
                                }
                                NumberPickerSlot(value = pomodoroRounds, range = 1..10, unit = "輪", modifier = Modifier.weight(1f)) { 
                                    pomodoroRounds = it 
                                }
                            } else if (timerMode == TimerMode.COUNT_DOWN) {
                                val h = countDownTotalTime / 3600
                                val m = (countDownTotalTime % 3600) / 60
                                val s = countDownTotalTime % 60
                                
                                NumberPickerSlot(value = h, range = 0..23, unit = "時", modifier = Modifier.weight(1f)) { newH ->
                                    val total = newH * 3600 + m * 60 + s
                                    countDownTotalTime = total
                                    if(!countDownStarted) countDownTimeLeft = total
                                }
                                NumberPickerSlot(value = m, range = 0..59, unit = "分", modifier = Modifier.weight(1f)) { newM ->
                                    val total = h * 3600 + newM * 60 + s
                                    countDownTotalTime = total
                                    if(!countDownStarted) countDownTimeLeft = total
                                }
                                NumberPickerSlot(value = s, range = 0..59, unit = "秒", modifier = Modifier.weight(1f)) { newS ->
                                    val total = h * 3600 + m * 60 + newS
                                    countDownTotalTime = total
                                    if(!countDownStarted) countDownTimeLeft = total
                                }
                            }
                        }
                    }
                }
            }
        }

        // 底部按鈕
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 60.dp),
            contentAlignment = Alignment.Center
        ) {
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

            if (!isRunning && !isStarted && timerMode != TimerMode.COUNT_UP) {
                // 開始專注按鈕 (膠囊風格，與繼續按鈕一致)
                Button(
                    onClick = {
                        when (timerMode) {
                            TimerMode.POMODORO -> { pomodoroIsRunning = true; pomodoroStarted = true }
                            TimerMode.COUNT_DOWN -> { countDownIsRunning = true; countDownStarted = true }
                            else -> {}
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = SparkleColorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth(0.5f).height(48.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (timerMode == TimerMode.POMODORO) "開始專注" else "開始計時",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 重置 (膠囊風格)
                    Button(
                        onClick = { showResetConfirmDialog = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = SparkleColorScheme.error
                        ),
                        modifier = Modifier.weight(1f).height(48.dp),
                        border = BorderStroke(1.dp, SparkleColorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(FeatherIcons.Square, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("重置", fontSize = 14.sp)
                    }
                    
                    // 開始/暫停 (膠囊風格)
                    val isRunning = when(timerMode) {
                        TimerMode.POMODORO -> pomodoroIsRunning
                        TimerMode.COUNT_UP -> countUpIsRunning
                        TimerMode.COUNT_DOWN -> countDownIsRunning
                    }
                    Button(
                        onClick = {
                            when (timerMode) {
                                TimerMode.POMODORO -> { pomodoroIsRunning = !pomodoroIsRunning; pomodoroStarted = true }
                                TimerMode.COUNT_UP -> { countUpIsRunning = !countUpIsRunning }
                                TimerMode.COUNT_DOWN -> { countDownIsRunning = !countDownIsRunning; countDownStarted = true }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.9f),
                            contentColor = SparkleColorScheme.primary
                        ),
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isRunning) "暫停" else "繼續", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    // 跳過 (膠囊風格)
                    Button(
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
                                } else if (pomodoroCurrentPhase == pomodoroRounds * 2 - 1) {
                                    // 最後一次休息跳過，直接完成
                                    saveProgress(TimerMode.POMODORO, pomodoroTotalTime - pomodoroTimeLeft)
                                    pomodoroIsRunning = false
                                    pomodoroTimeLeft = 0
                                    pomodoroIsResting = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("蕃茄鐘已完成！")
                                    }
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("不支援跳過")
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        enabled = timerMode == TimerMode.POMODORO,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = SparkleColorScheme.secondary,
                            disabledContainerColor = Color.White.copy(alpha = 0.05f),
                            disabledContentColor = Color.Gray
                        ),
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
}

@Composable
@Preview
fun FocusTimerScreenPreview(){
    FocusTimerScreen()

}