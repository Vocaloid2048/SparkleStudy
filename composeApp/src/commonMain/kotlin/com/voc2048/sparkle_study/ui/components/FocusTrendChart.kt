package com.voc2048.sparkle_study.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

data class ChartData(
    val label: String,
    val value: Float
)

@Composable
fun FocusTrendChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = data.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 60f // Default max 60 mins

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = (canvasWidth / (data.size * 2))
            val spacing = barWidth

            data.forEachIndexed { index, item ->
                val barHeight = (item.value / maxValue) * canvasHeight
                val xOffset = spacing / 2 + index * (barWidth + spacing)
                val yOffset = canvasHeight - barHeight

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(xOffset, yOffset),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                
                // Draw value on top if needed
                if (item.value > 0) {
                    val textLayoutResult = textMeasurer.measure(
                        text = item.value.toInt().toString(),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = barColor)
                    )
                    drawText(
                        textLayoutResult,
                        topLeft = Offset(
                            xOffset + (barWidth - textLayoutResult.size.width) / 2,
                            yOffset - textLayoutResult.size.height - 2.dp.toPx()
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEach { item ->
                Text(
                    text = item.label,
                    fontSize = 10.sp,
                    color = labelColor,
                    modifier = Modifier.width(IntrinsicSize.Min)
                )
            }
        }
    }
}
