package com.voc2048.sparkle_study.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voc2048.sparkle_study.ui.components.AccountCard
import com.voc2048.sparkle_study.ui.components.SettingsGroup
import com.voc2048.sparkle_study.ui.components.SettingsItem
import com.voc2048.sparkle_study.getAppInfo
import compose.icons.FeatherIcons
import compose.icons.feathericons.*

@Composable
fun SettingsScreen() {
    val appInfo = getAppInfo()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "設定",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            AccountCard(
                username = "用戶名稱",
                title = "資深專注者",
                level = 12,
                onClick = { /* 帳戶設定 */ }
            )
        }

        item {
            SettingsGroup(title = "偏好設定") {
                SettingsItem(
                    icon = FeatherIcons.Bell, 
                    title = "通知設定",
                    onClick = { /* 通知設定 */ }
                )
                SettingsItem(
                    icon = FeatherIcons.Settings, 
                    title = "通用設定",
                    onClick = { /* 通用設定 */ }
                )
            }
        }

        item {
            SettingsGroup(title = "支援與關於") {
                SettingsItem(
                    icon = FeatherIcons.Info, 
                    title = "關於 SparkleStudy",
                    onClick = { /* 關於 */ }
                )
            }
        }

        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "版本 ${appInfo.version} (Build ${appInfo.buildNumber})",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}


@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}