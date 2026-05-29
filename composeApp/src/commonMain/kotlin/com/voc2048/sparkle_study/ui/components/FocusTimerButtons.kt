package com.voc2048.sparkle_study.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.voc2048.sparkle_study.utils.SparkleColorScheme
import com.voc2048.sparkle_study.utils.hazeEffectSparkle
import dev.chrisbanes.haze.HazeState

@Composable
fun ActionButton(
    icon: ImageVector,
    tint: Color,
    hazeState: HazeState,
    onClick: () -> Unit
) {
    val size = 64.dp
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
    }
}
