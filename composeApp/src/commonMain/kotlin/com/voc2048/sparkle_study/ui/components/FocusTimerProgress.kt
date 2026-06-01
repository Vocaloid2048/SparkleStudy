package com.voc2048.sparkle_study.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voc2048.sparkle_study.utils.SparkleColorScheme
import com.voc2048.sparkle_study.utils.UtilsTools.formatSecondsToTimerString
import com.voc2048.sparkle_study.utils.UtilsTools.toRadians
import com.voc2048.sparkle_study.utils.hazeEffectSparkle
import dev.chrisbanes.haze.HazeState
import files.Res
import files.tomato
import org.jetbrains.compose.resources.painterResource
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FocusTimerProgress(
    displayTime: Int,
    progress: Float,
    isRunning: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    isReverse: Boolean = false,
    needShadow: Boolean = false,
    indicatorIcon: String = "🔥",
    showText: Boolean = true,
    showTomato: Boolean = false
) {
    val scheme = SparkleColorScheme

    // 使用 isRunning 來增加動態感：指示器縮放動畫
    val infiniteTransition = rememberInfiniteTransition(label = "IndicatorPulse")
    val indicatorScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    Box(
        modifier = modifier.size(320.dp),
        contentAlignment = Alignment.Center
    ) {
        // isReverse = true (預設): 進度條隨時間減少而縮短 (從 100% 到 0%)
        // isReverse = false: 進度條隨時間減少而增長 (從 0% 到 100%)
        val sweepAngle = if (isReverse) {
            360f * progress
        } else {
            360f * (1f - progress)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.width / 2) - 24.dp.toPx()
            val strokeWidth = 10.dp.toPx()

            // 0. 繪製底色圓環
            drawCircle(
                color = scheme.outlineVariant,
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 1. 繪製進度條 (漸變色: error -> secondary -> primary)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(scheme.error, scheme.secondary, scheme.primary, scheme.error),
                    center = center
                ),
                startAngle = 270f,
                sweepAngle = if (isReverse) -sweepAngle else sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
        }

        // 2. Indicator Emoji
        Box(modifier = Modifier.fillMaxSize()) {
            val radiusDp = (320.dp / 2) - 24.dp
            val indicatorAngle = if (isReverse) 270f - sweepAngle else 270f + sweepAngle
            val indicatorRadians = toRadians(indicatorAngle.toDouble())
            Text(
                text = indicatorIcon,
                fontSize = 28.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer(
                        scaleX = indicatorScale,
                        scaleY = indicatorScale,
                        translationX = with(androidx.compose.ui.platform.LocalDensity.current) { (radiusDp * cos(indicatorRadians).toFloat()).toPx() },
                        translationY = with(androidx.compose.ui.platform.LocalDensity.current) { (radiusDp * sin(indicatorRadians).toFloat()).toPx() }
                    )
            )
        }

        // 3. 蕃茄背景
        if (showTomato) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(Res.drawable.tomato),
                    contentDescription = null,
                    modifier = Modifier.size(260.dp).alpha(0.8f)
                )
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(scheme.surface.copy(alpha = 0.4f), CircleShape)
                )
            }
        }

        // 4. 中央面板
        if (showText) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .hazeEffectSparkle(hazeState),
                contentAlignment = Alignment.Center
            ) {
                val timerText = formatSecondsToTimerString(displayTime)
                
                Box(contentAlignment = Alignment.Center) {
                    val shadowColor = scheme.primary.copy(alpha = 0.2f)
                    
                    if(needShadow){
                        listOf(
                            Offset(-1f, -1f), Offset(1f, -1f),
                            Offset(-1f, 1f), Offset(1f, 1f)
                        ).forEach { offset ->
                            Text(
                                text = timerText,
                                fontSize = 54.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = shadowColor,
                                letterSpacing = 2.sp,
                                modifier = Modifier.offset(offset.x.dp, offset.y.dp)
                            )
                        }
                    }

                    Text(
                        text = timerText,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onBackground,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
