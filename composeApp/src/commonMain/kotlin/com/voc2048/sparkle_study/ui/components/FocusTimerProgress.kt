package com.voc2048.sparkle_study.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voc2048.sparkle_study.utils.SparkleColorScheme
import com.voc2048.sparkle_study.utils.UtilsTools.formatSecondsToTimerString
import com.voc2048.sparkle_study.utils.UtilsTools.toRadians
import com.voc2048.sparkle_study.utils.hazeEffectSparkle
import dev.chrisbanes.haze.HazeState
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FocusTimerProgress(
    timeLeft: Int,
    totalTime: Int,
    isRunning: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    isReverse: Boolean = false,
    needShadow: Boolean = false
) {
    // 粒子數據類別
    data class SparkleParticle(
        val id: Int,
        val xOffset: Float,
        val yOffset: Float,
        val size: Float,
        val alpha: Float,
        val color: Color,
        val rotation: Float,
        val shapeType: Int // 0: 圓形, 1: 菱形/星形
    )

    val infiniteTransition = rememberInfiniteTransition(label = "SparkleTransition")
    val particleTick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ParticleTick"
    )

    var particles by remember { mutableStateOf(listOf<SparkleParticle>()) }

    val scheme = SparkleColorScheme
    val particleSecondary = scheme.secondary
    val particleError = scheme.error
    val density = androidx.compose.ui.platform.LocalDensity.current

    LaunchedEffect(timeLeft, particleTick) {
        if (isRunning) {
            val newParticles = (0..3).map {
                SparkleParticle(
                    id = Random.nextInt(),
                    xOffset = (Random.nextFloat() - 0.5f) * with(density) { 45.dp.toPx() },
                    yOffset = (Random.nextFloat() - 0.5f) * with(density) { 45.dp.toPx() },
                    size = with(density) { Random.nextFloat() * 8.dp.toPx() + 4.dp.toPx() },
                    alpha = Random.nextFloat() * 0.7f + 0.3f,
                    color = if (Random.nextBoolean()) particleSecondary else particleError,
                    rotation = Random.nextFloat() * 360f,
                    shapeType = Random.nextInt(2)
                )
            }
            particles = (newParticles + particles.map { it.copy(alpha = it.alpha * 0.8f, size = it.size * 0.95f) })
                .filter { it.alpha > 0.1f }
                .take(40)
        } else {
            particles = emptyList()
        }
    }

    Box(
        modifier = modifier.size(320.dp),
        contentAlignment = Alignment.Center
    ) {
        val progress = timeLeft.toFloat() / totalTime.toFloat()
        
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

            // 1. 繪製進度條 (主題色淡化 0.8f)
            // isReverse = true: 逆時針縮短 (startAngle = 270, sweep = -sweepAngle)
            // isReverse = false: 順時針增長 (startAngle = 270, sweep = sweepAngle)
            drawArc(
                color = scheme.primary.copy(alpha = 0.8f),
                startAngle = 270f,
                sweepAngle = if (isReverse) -sweepAngle else sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )

            // 2. 計算 Indicator 指標座標 (位於 sweepAngle 結束處)
            val indicatorAngle = if (isReverse) 270f - sweepAngle else 270f + sweepAngle
            val indicatorRadians = toRadians(indicatorAngle.toDouble())
            val indicatorX = center.x + radius * cos(indicatorRadians).toFloat()
            val indicatorY = center.y + radius * sin(indicatorRadians).toFloat()

            // 3. 動態渲染火花粒子
            particles.forEach { particle ->
                rotate(particle.rotation, Offset(indicatorX + particle.xOffset, indicatorY + particle.yOffset)) {
                    if (particle.shapeType == 0) {
                        drawCircle(
                            color = particle.color.copy(alpha = particle.alpha),
                            radius = particle.size / 2,
                            center = Offset(indicatorX + particle.xOffset, indicatorY + particle.yOffset)
                        )
                    } else {
                        val pSize = particle.size
                        val path = Path().apply {
                            moveTo(indicatorX + particle.xOffset, indicatorY + particle.yOffset - pSize)
                            lineTo(indicatorX + particle.xOffset + pSize / 2, indicatorY + particle.yOffset)
                            lineTo(indicatorX + particle.xOffset, indicatorY + particle.yOffset + pSize)
                            lineTo(indicatorX + particle.xOffset - pSize / 2, indicatorY + particle.yOffset)
                            close()
                        }
                        drawPath(path, particle.color.copy(alpha = particle.alpha))
                    }
                }
            }
        }

        // 4. Indicator Emoji (🔥)
        Box(modifier = Modifier.fillMaxSize()) {
            val radiusDp = (320.dp / 2) - 24.dp
            val indicatorAngle = if (isReverse) 270f - sweepAngle else 270f + sweepAngle
            val indicatorRadians = toRadians(indicatorAngle.toDouble())
            Text(
                text = "🔥",
                fontSize = 28.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = radiusDp * cos(indicatorRadians).toFloat(),
                        y = radiusDp * sin(indicatorRadians).toFloat()
                    )
            )
        }

        // 中央霧化計時面板
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .hazeEffectSparkle(hazeState) // 應用霧化
                .background(scheme.surface.copy(alpha = 0.25f))
                .border(1.5.dp, scheme.surface.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val timerText = formatSecondsToTimerString(timeLeft)
            
            // 使用者要求：計時文字添加2dp的主題色+black.alpha(0.8f) 的border
            Box(contentAlignment = Alignment.Center) {
                val shadowColor = Color.Black.copy(alpha = 0.8f)
                
                // 模擬描邊 (上下左右偏移 2dp)
                if(needShadow){
                    listOf(
                        Offset(-2f, -2f), Offset(2f, -2f),
                        Offset(-2f, 2f), Offset(2f, 2f),
                        Offset(0f, -2f), Offset(0f, 2f),
                        Offset(-2f, 0f), Offset(2f, 0f)
                    ).forEach { offset ->
                        Text(
                            text = timerText,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = shadowColor,
                            letterSpacing = 2.sp,
                            modifier = Modifier.offset(offset.x.dp, offset.y.dp)
                        )
                    }
                }

                Text(
                    text = timerText,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
