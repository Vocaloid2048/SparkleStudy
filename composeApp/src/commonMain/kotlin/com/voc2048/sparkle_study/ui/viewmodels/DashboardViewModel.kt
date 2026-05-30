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
