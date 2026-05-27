package com.voc2048.sparkle_study.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.voc2048.sparkle_study.types.AvatarItem

/**
 * 用於渲染 2D 虛擬角色的組件，支持多層貼圖堆疊。
 */
@Composable
fun AvatarView(
    equippedItems: List<AvatarItem>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 這裡未來會根據 equippedItems 的 assetPath 進行層級繪製
        Text("Avatar (Layers: ${equippedItems.size})")
    }
}
