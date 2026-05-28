package com.voc2048.sparkle_study.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voc2048.sparkle_study.ui.components.FocusTimerProgress
import com.voc2048.sparkle_study.ui.components.FocusTimerWater
import com.voc2048.sparkle_study.utils.SparkleColorScheme
import com.voc2048.sparkle_study.utils.UtilsTools.formatSecondsToTimerString
import com.voc2048.sparkle_study.utils.UtilsTools.toRadians
import com.voc2048.sparkle_study.utils.hazeEffectSparkle
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import files.Res
import files.test_img
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen() {
    var totalTime by remember { mutableStateOf(25 * 60) } //預設25分鐘
    var timeLeft by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val hazeState = rememberHazeState()

    // 倒數計時器核心協程
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            isRunning = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState),
        contentAlignment = Alignment.Center
    ) {
        // 背景改為主題色漸變底部導航欄顔色
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SparkleColorScheme.primary.copy(alpha = 0.5f),
                            SparkleColorScheme.background
                        )
                    )
                )
        )

        // 頂部導航切換列
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 20.dp)
                .hazeEffectSparkle(hazeState)
                .background(SparkleColorScheme.background.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(4.dp) // 使用者要求：padding = 4.dp
        ) {
            val tabs = listOf("番茄鐘", "正計時", "倒計時", "水模式")
            
            // Liquid Glass 動畫背景
            val indicatorOffset by animateDpAsState(
                targetValue = when (selectedTab) {
                    0 -> 0.dp
                    1 -> 85.dp
                    2 -> 170.dp
                    else -> 255.dp
                },
                animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy)
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .size(width = 85.dp, height = 40.dp)
                    .background(SparkleColorScheme.primary, RoundedCornerShape(20.dp))
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .size(width = 85.dp, height = 40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null // 移除 ripple
                            ) { selectedTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) SparkleColorScheme.onPrimary else SparkleColorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 中央核心計時區域
        if (selectedTab == 3) {
            FocusTimerWater(
                timeLeft = timeLeft,
                totalTime = totalTime,
                isRunning = isRunning,
                hazeState = hazeState,
                isTimerWaving = false
            )
        } else {
            FocusTimerProgress(
                timeLeft = timeLeft,
                totalTime = totalTime,
                isRunning = isRunning,
                hazeState = hazeState,
                isReverse = false // 番茄鐘與倒計時使用 Reverse
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .wrapContentSize()
                .padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val leftRightTintColor = SparkleColorScheme.primary.copy(alpha = 0.8f) // 主題色+black.alpha(0.8f) 的模擬
            
            // 左側：配置按鈕 背景用白色 添加霧化
            IconButton(
                onClick = { /* 開啟設定 */ },
                modifier = Modifier
                    .background(SparkleColorScheme.background.copy(alpha = 0.8f), CircleShape)
                    .hazeEffectSparkle(hazeState)
                    .size(56.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = leftRightTintColor)
            }

            // 中間：翡翠綠開始/暫停按鈕 (僅icon)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SparkleColorScheme.primary)
                    .hazeEffectSparkle(hazeState)
                    .clickable { isRunning = !isRunning },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    tint = SparkleColorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // 右側：跳過/重設按鈕 背景用白色 添加霧化
            IconButton(
                onClick = {
                    if (isRunning) {
                        timeLeft = 0
                        isRunning = false
                    } else {
                        timeLeft = totalTime
                    }
                },
                modifier = Modifier
                    .background(SparkleColorScheme.background.copy(alpha = 0.8f), CircleShape)
                    .hazeEffectSparkle(hazeState)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.SkipNext else Icons.Default.Refresh,
                    contentDescription = if (isRunning) "Skip" else "Reset",
                    tint = leftRightTintColor
                )
            }
        }
    }
}

@Composable
@Preview
fun FocusTimerScreenPreview(){
    FocusTimerScreen()
}