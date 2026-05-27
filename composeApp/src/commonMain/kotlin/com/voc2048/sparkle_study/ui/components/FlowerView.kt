package com.voc2048.sparkle_study.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.voc2048.sparkle_study.types.Flower

/**
 * 用於渲染花卉成長狀態的組件。
 */
@Composable
fun FlowerView(
    flower: Flower,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 這裡未來會展示不同成長階段的 2D 資源
        Text("Flower: ${flower.name} (${flower.currentStatus})")
    }
}
