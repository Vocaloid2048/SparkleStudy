package com.voc2048.sparkle_study.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.voc2048.sparkle_study.utils.hazeEffectSparkle
import dev.chrisbanes.haze.HazeState

/**
 * 全域通用的彈窗組件，支援毛玻璃效果。
 */
@Composable
fun AppDialog(
    titleString: String,
    hazeState: HazeState,
    isPopupShow: MutableState<Boolean>,
    components: @Composable ColumnScope.() -> Unit
) {
    if (isPopupShow.value) {
        Dialog(
            onDismissRequest = { isPopupShow.value = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(32.dp))
                    .hazeEffectSparkle(state = hazeState)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = titleString,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    components()
                    
                    Button(
                        onClick = { isPopupShow.value = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("完成")
                    }
                }
            }
        }
    }
}
