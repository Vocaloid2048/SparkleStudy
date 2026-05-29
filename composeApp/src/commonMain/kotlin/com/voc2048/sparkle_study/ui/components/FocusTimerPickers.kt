package com.voc2048.sparkle_study.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voc2048.sparkle_study.utils.SparkleColorScheme
import kotlinx.coroutines.launch

@Composable
fun NumberPickerSlot(
    value: Int,
    range: IntRange = 1..120,
    unit: String = "",
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    val itemHeight = 50.dp
    val state = rememberLazyListState(initialFirstVisibleItemIndex = (value - range.first))
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) {
            val centerIndex = state.firstVisibleItemIndex
            val newValue = (range.first + centerIndex).coerceIn(range)
            if (newValue != value) {
                onValueChange(newValue)
            }
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
        ) {
            items(range.last - range.first + 1) { index ->
                val num = range.first + index
                val isSelected = num == value
                
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch {
                                state.animateScrollToItem(index)
                                onValueChange(num)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = num.toString().padStart(2, '0'),
                            fontSize = if (isSelected) 38.sp else 30.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                        )
                        if (isSelected && unit.isNotEmpty()) {
                            Text(
                                text = unit,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
