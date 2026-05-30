package com.voc2048.sparkle_study.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voc2048.sparkle_study.ui.viewmodels.StudyViewModel

@Composable
fun DashboardScreen(viewModel: StudyViewModel = viewModel { StudyViewModel() }) {
    val user by viewModel.user.collectAsState()
    
    val focusHours = (user?.totalFocusMinutes ?: 0) / 60
    val focusMins = (user?.totalFocusMinutes ?: 0) % 60
    val timeDisplay = "${focusHours.toString().padStart(2, '0')}h ${focusMins.toString().padStart(2, '0')}m"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "今日成長",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 核心統計卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("累計專注時間", color = Color.White.copy(alpha = 0.8f))
                    Text(timeDisplay, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem("持有代幣", user?.coins?.toString() ?: "0")
                        StatItem("持有養分", user?.nutrient?.toString() ?: "0")
                        StatItem("連擊天數", user?.loginStreak?.toString() ?: "1")
                    }
                }
            }
        }

        // 趨勢圖佔位
        item {
            Text("專注趨勢", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("圖表載入中...", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Preview
@Composable
fun DashboardScreenPreview(){
    DashboardScreen()
}
