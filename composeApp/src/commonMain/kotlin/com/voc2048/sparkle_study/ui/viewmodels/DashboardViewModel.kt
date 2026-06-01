package com.voc2048.sparkle_study.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voc2048.sparkle_study.database.DatabaseHelper
import com.voc2048.sparkle_study.ui.components.ChartData
import kotlinx.coroutines.flow.*
import com.voc2048.sparkle_study.database.DailyTaskEntity
import com.voc2048.sparkle_study.database.PlantEntity
import com.voc2048.sparkle_study.database.UserEntity
import com.voc2048.sparkle_study.utils.Preferences
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import com.voc2048.sparkle_study.getUptimeMillis
import com.voc2048.sparkle_study.utils.UtilsTools
import kotlin.time.Clock
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault

class DashboardViewModel : ViewModel() {
    private val db = DatabaseHelper.database
    private val focusDao = db.focusDao()
    private val taskDao = db.dailyTaskDao()
    private val plantDao = db.plantDao()
    private val userDao = db.userDao()
    private val transactionDao = db.coinTransactionDao()
    private val prefs = Preferences()

    val user: StateFlow<UserEntity?> = userDao.getUser()
        .onEach { 
            if (it != null && !isLoginChecking) {
                checkDailyLogin(it)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activePlant: StateFlow<PlantEntity?> = plantDao.getAllPlants()
        .map { it.firstOrNull { p -> p.status != "BLOOMING" && p.status != "DORMANT" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayTasks: StateFlow<List<DailyTaskEntity>> = taskDao.getTasksForDate(getTodayKey())
        .onEach { if (it.isEmpty()) initDailyTasks() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyGoalMinutes: StateFlow<Int> = MutableStateFlow(prefs.dailyGoalMinutes)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), prefs.dailyGoalMinutes)

    private val _isWeeklyMode = MutableStateFlow(false) 
    val isWeeklyMode: StateFlow<Boolean> = _isWeeklyMode.asStateFlow()

    private val _showTimeTamperDialog = MutableStateFlow(false)
    val showTimeTamperDialog: StateFlow<Boolean> = _showTimeTamperDialog.asStateFlow()

    private var isLoginChecking = false

    fun dismissTimeTamperDialog() {
        _showTimeTamperDialog.value = false
    }

    fun toggleChartMode() {
        _isWeeklyMode.value = !_isWeeklyMode.value
    }

    val weeklyFocusData: StateFlow<List<ChartData>> = combine(focusDao.getAllSessions(), _isWeeklyMode) { sessions, isWeekly ->
        val now = Clock.System.now().toLocalDateTime(currentSystemDefault()).date
        val daysToDisplay = if (isWeekly) {
            val currentDayOfWeek = now.dayOfWeek.ordinal 
            (0..6).map { now.minus(currentDayOfWeek - it, DateTimeUnit.DAY) }
        } else {
            (0..6).reversed().map { now.minus(it, DateTimeUnit.DAY) }
        }
        daysToDisplay.map { date ->
            val totalMinutes = sessions.filter { session ->
                val sessionDate = Instant.fromEpochMilliseconds(session.startTime)
                    .toLocalDateTime(currentSystemDefault()).date
                sessionDate == date
            }.sumOf { it.duration }
            ChartData(
                label = if (date == now) "今日" else "${date.month.ordinal + 1}/${date.dayOfMonth}",
                value = totalMinutes.toFloat()
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayFocusMinutes: StateFlow<Int> = weeklyFocusData.map { data ->
        data.lastOrNull { it.label == "今日" }?.value?.toInt() ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayEarnings: StateFlow<Pair<Int, Int>> = transactionDao.getAllTransactions()
        .map { txs ->
            val todayStart = getTodayStartMillis()
            val coins = txs.filter { it.createdAt >= todayStart && it.amount > 0 && it.type != "NUTRIENT" }.sumOf { it.amount }
            val nutrient = txs.filter { it.createdAt >= todayStart && it.type == "NUTRIENT" }.sumOf { it.amount }
            coins to nutrient
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0 to 0)

    private fun getTodayKey(): String {
        val now = Clock.System.now().toLocalDateTime(currentSystemDefault()).date
        return "${now.year}-${(now.month.ordinal + 1).toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
    }

    private fun getTodayStartMillis(): Long {
        val now = Clock.System.now().toLocalDateTime(currentSystemDefault()).date
        return now.atStartOfDayIn(currentSystemDefault()).toEpochMilliseconds()
    }

    private fun checkDailyLogin(user: UserEntity?) {
        if (user == null || isLoginChecking) return
        isLoginChecking = true
        
        viewModelScope.launch {
            try {
                val networkTime = UtilsTools.getNetworkTime()
                val currentSystemTime = Clock.System.now().toEpochMilliseconds()
                val currentUptime = getUptimeMillis()
                
                // 優先使用網路時間進行校驗
                val verifiedTime = if (networkTime != null) {
                    if (kotlin.math.abs(networkTime - currentSystemTime) > 300000) {
                        println("系統時鐘與網路不符，使用網路時間校準。")
                        networkTime
                    } else {
                        currentSystemTime
                    }
                } else {
                    if (prefs.lastSystemTime > 0 && prefs.lastUptime > 0) {
                        val systemDelta = currentSystemTime - prefs.lastSystemTime
                        val uptimeDelta = currentUptime - prefs.lastUptime
                        
                        if (currentUptime > prefs.lastUptime) {
                            if (systemDelta < -600000 || (systemDelta - uptimeDelta) > 600000) {
                                println("偵測到系統時鐘可能被手動更改！暫停獎勵發放。")
                                _showTimeTamperDialog.value = true
                                isLoginChecking = false // 允許修正後重新檢查
                                return@launch
                            }
                        }
                    }
                    currentSystemTime
                }

                val today = Instant.fromEpochMilliseconds(verifiedTime)
                    .toLocalDateTime(currentSystemDefault()).date.toString()

                // 二次檢查：防止在網路請求期間已經有其他地方更新了日期
                if (prefs.lastLoginDate == today) {
                    return@launch
                }

                // 立即記錄最後同步時間
                prefs.lastSystemTime = currentSystemTime
                prefs.lastUptime = currentUptime
                
                val lastDate = prefs.lastLoginDate
                // ！！！注意：必須在執行寫入資料庫前更新 lastLoginDate，防止併發重複領取
                prefs.lastLoginDate = today
                
                var newStreak = user.loginStreak
                if (lastDate.isNotEmpty()) {
                    val lastLocalDate = LocalDate.parse(lastDate)
                    val todayDate = LocalDate.parse(today)
                    val daysDiff = lastLocalDate.daysUntil(todayDate)
                    
                    when {
                        daysDiff == 1 -> newStreak++
                        daysDiff > 1 -> newStreak = 1
                        daysDiff < 0 -> {
                            // 偵測到時間倒流 (Time Travel Backwards)
                            println("偵測到日期倒流，不更新連續天數。")
                            _showTimeTamperDialog.value = true
                            // 回滾日期記錄，強制使用者修正
                            prefs.lastLoginDate = lastDate 
                            return@launch
                        }
                    }
                } else {
                    newStreak = 1
                }

                // Award coins based on streak (cycle of 7)
                val rewards = listOf(5, 10, 5, 15, 5, 20, 50)
                val rewardIndex = (newStreak - 1) % 7
                val rewardAmount = rewards[rewardIndex]

                // 執行獎勵發放 (一次性操作)
                userDao.earnCoins(
                    userId = user.id,
                    amount = rewardAmount,
                    type = "DAILY_LOGIN",
                    description = "每日登入獎勵 (第 $newStreak 天)",
                    refId = today,
                    transactionDao = transactionDao
                )

                userDao.insertOrUpdateUser(user.copy(
                    loginStreak = newStreak,
                    coins = user.coins + rewardAmount,
                    lastSyncAt = verifiedTime
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // 不重置 isLoginChecking，確保當次生命週期只跑一次
            }
        }
    }

    fun claimTask(taskId: String) {
        viewModelScope.launch {
            val task = todayTasks.value.find { it.taskId == taskId }
            if (task != null && task.currentProgress >= task.targetValue && !task.isClaimed) {
                taskDao.insertOrUpdateTask(task.copy(isClaimed = true))
                val userId = user.value?.id ?: "default_user"
                userDao.earnCoins(
                    userId = userId,
                    amount = 20,
                    type = "TASK_REWARD",
                    description = "完成任務: ${task.taskType}",
                    refId = taskId,
                    transactionDao = transactionDao
                )
            }
        }
    }

    private fun initDailyTasks() {
        viewModelScope.launch {
            val dateKey = getTodayKey()
            val defaultTasks = listOf(
                DailyTaskEntity("task_water", "WATER", 0, 3, false, dateKey),
                DailyTaskEntity("task_bottle", "BOTTLE", 0, 1, false, dateKey),
                DailyTaskEntity("task_focus_60", "FOCUS_60", 0, 1, false, dateKey)
            )
            defaultTasks.forEach { taskDao.insertOrUpdateTask(it) }
        }
    }

    val hourlyDistribution: StateFlow<List<FocusDistribution>> = combine(focusDao.getAllSessions(), _isWeeklyMode) { sessions, isWeekly ->
        val now = Clock.System.now().toLocalDateTime(currentSystemDefault()).date
        
        val dates = if (isWeekly) {
            val isoOrdinal = now.dayOfWeek.ordinal // Mon=0, ..., Sun=6
            val sunBasedOrdinal = (isoOrdinal + 1) % 7 // Sun=0, ..., Sat=6
            (0..6).map { now.minus(sunBasedOrdinal - it, DateTimeUnit.DAY) }
        } else {
            // Last 7 days, chronological
            (0..6).reversed().map { now.minus(it, DateTimeUnit.DAY) }
        }

        val dayNames = listOf("週日", "週一", "週二", "週三", "週四", "週五", "週六")

        dates.flatMap { date ->
            val dateLabel = if (isWeekly) {
                dayNames[(date.dayOfWeek.ordinal + 1) % 7]
            } else {
                "${(date.month.ordinal + 1).toString().padStart(2, '0')}/${date.dayOfMonth.toString().padStart(2, '0')}"
            }
            (0..23).map { hour ->
                val start = date.atTime(hour, 0).toInstant(currentSystemDefault()).toEpochMilliseconds()
                val end = date.atTime(hour, 59, 59).toInstant(currentSystemDefault()).toEpochMilliseconds()
                val focusMins = sessions.filter { it.startTime in start..end }.sumOf { it.duration }
                FocusDistribution(date, hour, focusMins, 0, 0, dateLabel)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

data class FocusDistribution(val date: LocalDate, val hour: Int, val focusMins: Int, val restMins: Int, val idleMins: Int, val label: String)
