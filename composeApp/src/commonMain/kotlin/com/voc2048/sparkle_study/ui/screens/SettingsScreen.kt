package com.voc2048.sparkle_study.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import com.voc2048.sparkle_study.BuildKonfig
import com.voc2048.sparkle_study.ui.components.AccountCard
import com.voc2048.sparkle_study.ui.components.SettingsGroup
import com.voc2048.sparkle_study.ui.components.SettingsItem
import com.voc2048.sparkle_study.ui.viewmodels.TimerSwitchBehavior
import com.voc2048.sparkle_study.utils.Preferences
import com.voc2048.sparkle_study.utils.ThemeMode
import com.voc2048.sparkle_study.utils.appThemeState
import compose.icons.FeatherIcons
import compose.icons.feathericons.*

@Composable
fun SettingsScreen() {
    val prefs = remember { Preferences() }

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
                "設定",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            AccountCard(
                username = prefs.userName,
                title = "資深專注者",
                level = 12,
                onClick = { /* 帳戶設定 */ }
            )
        }

        item {
            SettingsGroup(title = "偏好設定") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("外觀主題", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = appThemeState.value == mode,
                                    onClick = { 
                                        appThemeState.value = mode 
                                        prefs.themeMode = mode.name
                                    }
                                )
                                val text = when (mode) {
                                    ThemeMode.LIGHT -> "淺色"
                                    ThemeMode.DARK -> "深色"
                                    ThemeMode.SYSTEM -> "自動"
                                }
                                Text(
                                    text, 
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable { 
                                        appThemeState.value = mode 
                                        prefs.themeMode = mode.name
                                    }
                                )
                            }
                        }
                    }
                }

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
            SettingsGroup(title = "計時器設定") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("切換模式行為", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = TimerSettings.switchBehavior == TimerSwitchBehavior.CONFIRM,
                            onClick = { 
                                TimerSettings.switchBehavior = TimerSwitchBehavior.CONFIRM
                                prefs.timerSwitchBehavior = TimerSwitchBehavior.CONFIRM.name
                            }
                        )
                        Text("二次確認", modifier = Modifier.clickable { 
                            TimerSettings.switchBehavior = TimerSwitchBehavior.CONFIRM
                            prefs.timerSwitchBehavior = TimerSwitchBehavior.CONFIRM.name
                        })
                        
                        Spacer(modifier = Modifier.width(16.dp))

                        RadioButton(
                            selected = TimerSettings.switchBehavior == TimerSwitchBehavior.PAUSE,
                            onClick = { 
                                TimerSettings.switchBehavior = TimerSwitchBehavior.PAUSE
                                prefs.timerSwitchBehavior = TimerSwitchBehavior.PAUSE.name
                            }
                        )
                        Text("暫停", modifier = Modifier.clickable { 
                            TimerSettings.switchBehavior = TimerSwitchBehavior.PAUSE
                            prefs.timerSwitchBehavior = TimerSwitchBehavior.PAUSE.name
                        })
                    }
                }
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
                    BuildKonfig.appVersionName,
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
