package com.voc2048.sparkle_study.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voc2048.sparkle_study.utils.UtilsTools
import compose.icons.FeatherIcons
import compose.icons.feathericons.*

@Composable
fun FocusTimerScreen() {
    var isRunning by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(1500) } // 25 分鐘

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
        ) {
            TextButton(onClick = {}) { Text("番茄鐘", fontWeight = FontWeight.Bold) }
            TextButton(onClick = {}) { Text("正向計時", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
        }

        Spacer(modifier = Modifier.height(64.dp))

        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = UtilsTools.formatSecondsToTimerString(timeLeft),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "保持專注中",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { timeLeft = 1500 },
                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(FeatherIcons.RefreshCw, contentDescription = null, modifier = Modifier.size(24.dp))
            }

            LargeFloatingActionButton(
                onClick = { isRunning = !isRunning },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    if (isRunning) FeatherIcons.Pause else FeatherIcons.Play,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = { /* TODO: Settings */ },
                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(FeatherIcons.Settings, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
    }
}
