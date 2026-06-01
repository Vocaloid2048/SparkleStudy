package com.voc2048.sparkle_study.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import files.Res
import files.tomato
import com.voc2048.sparkle_study.utils.SparkleColorScheme
import com.voc2048.sparkle_study.utils.UtilsTools.formatSecondsToTimerString
import dev.chrisbanes.haze.HazeState
import com.voc2048.sparkle_study.utils.hazeEffectSparkle
import kotlin.math.sin

@Composable
fun FocusTimerWater(
    displayTime: Int,
    progress: Float,
    isRunning: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    isTimerWaving: Boolean = true,
    showText: Boolean = true,
    isTimerTextBack: Boolean = false, // New parameter: True means in front
    showTomato: Boolean = false
) {
    val scheme = SparkleColorScheme
    val primaryColor = scheme.primary

    // 頻率控制動畫 (0 to 10 in 2s, or back to 0)
    val waveFrequency by animateFloatAsState(
        targetValue = if (isRunning) 10f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
    )

    // 水波相位動畫
    val infiniteTransition = rememberInfiniteTransition(label = "WaveTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * kotlin.math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    // 飲管吸水動畫
    val strawSuckOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "StrawSuck"
    )

    Box(
        modifier = modifier.size(320.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. 魚缸背景 (底部背景)
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .hazeEffectSparkle(hazeState)
                .graphicsLayer(alpha = 0.3f)
        )

        // 蕃茄背景 (在水面之下，魚缸背景之上)
        if (showTomato) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(Res.drawable.tomato),
                    contentDescription = null,
                    modifier = Modifier.size(260.dp).alpha(0.8f)
                )
                // 淺白色前景 overlay (統一使用此效果)
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(scheme.surface.copy(alpha = 0.4f), CircleShape)
                )
            }
        }

        val timerText = formatSecondsToTimerString(displayTime)
        val waterLevelRatio = progress

        // 2. 倒數文字與光學扭曲 (如果 isTimerWaving 為 true 且 isTimerTextBack 為 false，則放在水後方)
        if (isTimerWaving && showText && !isTimerTextBack) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // 空氣中的文字 (上方)
                Box(modifier = Modifier.fillMaxSize().graphicsLayer {
                    clip = true
                    shape = object : androidx.compose.ui.graphics.Shape {
                        override fun createOutline(
                            size: androidx.compose.ui.geometry.Size,
                            layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                            density: androidx.compose.ui.unit.Density
                        ) = androidx.compose.ui.graphics.Outline.Rectangle(
                            Rect(0f, 0f, size.width, size.height * (1f - waterLevelRatio))
                        )
                    }
                }, contentAlignment = Alignment.Center) {
                    Text(
                        text = timerText,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onBackground,
                        letterSpacing = 2.sp
                    )
                }

                // 水中的文字 (下方，帶扭曲)
                Box(modifier = Modifier.fillMaxSize().graphicsLayer {
                    clip = true
                    shape = object : androidx.compose.ui.graphics.Shape {
                        override fun createOutline(
                            size: androidx.compose.ui.geometry.Size,
                            layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                            density: androidx.compose.ui.unit.Density
                        ) = androidx.compose.ui.graphics.Outline.Rectangle(
                            Rect(0f, size.height * (1f - waterLevelRatio), size.width, size.height)
                        )
                    }
                    if (isRunning) {
                        val distortion = sin(phase * 2f) * (waveFrequency / 10f) * 8f
                        translationX = distortion
                        scaleX = 1f + (distortion / 50f)
                        rotationZ = distortion / 4f
                    }
                }, contentAlignment = Alignment.Center) {
                    Text(
                        text = timerText,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onBackground.copy(alpha = 0.7f),
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // 3. 水波圖層 (魚缸內部)
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
        ) {
            val width = size.width
            val height = size.height
            val waterLevelY = height * (1f - waterLevelRatio)

            // 使用者要求：白色+主題色, 主題色x0.6, 白色+主題色x0.4，alpha = 0.8
            val layers = listOf(
                // 底層: 白色 + 主題色 x 0.4
                Triple(Color.White.mix(primaryColor, 0.4f).copy(alpha = 0.8f), 0.8f, 0f),
                // 中層: 主題色 x 0.6
                Triple(primaryColor.copy(alpha = 0.6f * 0.8f), 1.0f, 0.5f),
                // 頂層: 白色 + 主題色
                Triple(Color.White.mix(primaryColor, 1.0f).copy(alpha = 0.8f), 1.2f, 1.0f)
            )

            layers.forEach { layer ->
                val color = layer.first
                val amplitudeMultiplier = layer.second
                val phaseOffset = layer.third
                
                val path = Path().apply {
                    val baseAmplitude = 15f * (waveFrequency / 10f) * amplitudeMultiplier
                    moveTo(0f, height)
                    lineTo(0f, waterLevelY)
                    
                    for (x in 0..width.toInt() step 5) {
                        val waveX = x.toFloat()
                        val relativeX = waveX / width
                        val y = waterLevelY + sin(relativeX * 2 * kotlin.math.PI.toFloat() + phase + phaseOffset) * baseAmplitude
                        lineTo(waveX, y)
                    }
                    
                    lineTo(width, waterLevelY)
                    lineTo(width, height)
                    close()
                }

                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = color.alpha * 0.6f)),
                        startY = waterLevelY - 20f,
                        endY = height
                    )
                )
            }
        }

        // 4. 倒數文字 (放在水前方，暖可可色)
        if (showText && (!isTimerWaving || isTimerTextBack)) {
            Text(
                text = timerText,
                fontSize = 54.sp,
                fontWeight = FontWeight.ExtraBold,
                color = scheme.onBackground,
                letterSpacing = 2.sp
            )
        }

        // 5. 飲管 (更高、避開文字)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            // 飲管路徑：從水缸內部出發，經過折角，指向螢幕外
            // 調整起始點與折角座標，使其更偏向右上角，避開中央文字
            val elbowX = centerX + 130.dp.toPx()
            val elbowY = centerY - 160.dp.toPx()

            val strawPath = Path().apply {
                moveTo(centerX + 90.dp.toPx(), centerY - 40.dp.toPx())
                lineTo(elbowX, elbowY)
                lineTo(size.width + 100f, -50f)
            }

            drawPath(
                path = strawPath,
                color = scheme.outline.copy(alpha = 0.8f),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
            
            if (isRunning) {
                clipPath(strawPath) {
                    val dashSize = 25.dp.toPx()
                    val gapSize = 15.dp.toPx()
                    drawPath(
                        path = strawPath,
                        color = primaryColor.copy(alpha = 0.4f),
                        style = Stroke(
                            width = 5.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(dashSize, gapSize),
                                -strawSuckOffset * 6f
                            )
                        )
                    )
                }
            }
        }
        
        // 6. 魚缸高光與邊框
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .graphicsLayer { alpha = 0.5f }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, scheme.outlineVariant.copy(alpha = 0.3f)),
                        center = center,
                        radius = size.width / 2
                    )
                )
            }
        }
    }
}

private fun Color.mix(other: Color, amount: Float): Color {
    return Color(
        red = this.red * (1 - amount) + other.red * amount,
        green = this.green * (1 - amount) + other.green * amount,
        blue = this.blue * (1 - amount) + other.blue * amount,
        alpha = this.alpha * (1 - amount) + other.alpha * amount
    )
}
