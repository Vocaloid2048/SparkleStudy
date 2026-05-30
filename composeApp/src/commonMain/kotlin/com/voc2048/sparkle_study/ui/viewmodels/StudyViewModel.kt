package com.voc2048.sparkle_study.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voc2048.sparkle_study.database.AppDatabase
import com.voc2048.sparkle_study.database.DatabaseHelper
import com.voc2048.sparkle_study.database.FocusSessionEntity
import com.voc2048.sparkle_study.database.UserEntity
import com.voc2048.sparkle_study.utils.Preferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Clock

enum class TimerMode {
    POMODORO, COUNT_UP, COUNT_DOWN
}

enum class TimerSwitchBehavior {
    CONFIRM, PAUSE
}

class StudyViewModel : ViewModel() {
    private val db: AppDatabase = DatabaseHelper.database
    private val prefs = Preferences()

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    // --- Active State ---
    private val _timerMode = MutableStateFlow(TimerMode.valueOf(prefs.timerMode))
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // --- Pomodoro Specific ---
    private val _pomodoroPhase = MutableStateFlow(prefs.pomodoroCurrentPhase)
    val pomodoroPhase: StateFlow<Int> = _pomodoroPhase.asStateFlow()

    private val _isResting = MutableStateFlow(prefs.pomodoroIsResting)
    val isResting: StateFlow<Boolean> = _isResting.asStateFlow()

    private var timerJob: Job? = null

    init {
        _timeLeft.value = getCurrentModeTimeFromPrefs()
        
        viewModelScope.launch {
            db.userDao().getUser().collect {
                if (it == null) createDefaultUser() else _user.value = it
            }
        }
        restoreTimerState()
    }

    private fun getCurrentModeTimeFromPrefs(): Int {
        return when (TimerMode.valueOf(prefs.timerMode)) {
            TimerMode.POMODORO -> prefs.pomodoroCurrentSeconds
            TimerMode.COUNT_UP -> prefs.countupCurrentSeconds
            TimerMode.COUNT_DOWN -> prefs.countdownCurrentSeconds
        }
    }

    private fun restoreTimerState() {
        if (prefs.isActiveSessionRunning) {
            val now = Clock.System.now().toEpochMilliseconds()
            val diff = ((now - prefs.lastActiveTimestamp) / 1000L).toInt()
            
            val mode = _timerMode.value
            if (mode == TimerMode.COUNT_UP) {
                _timeLeft.value = prefs.countupCurrentSeconds + diff
                startTimer()
            } else {
                val current = if (mode == TimerMode.POMODORO) prefs.pomodoroCurrentSeconds else prefs.countdownCurrentSeconds
                val remaining = current - diff
                if (remaining > 0) {
                    _timeLeft.value = remaining
                    startTimer()
                } else {
                    completeTimer()
                }
            }
        }
    }

    fun startTimer() {
        if (_isRunning.value) return
        _isRunning.value = true
        saveTimerToPrefs()
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRunning.value) {
                delay(1000)
                if (_timerMode.value == TimerMode.COUNT_UP) {
                    _timeLeft.value++
                } else {
                    if (_timeLeft.value > 0) {
                        _timeLeft.value--
                    } else {
                        completeTimer()
                        break
                    }
                }
                saveTimerToPrefs()
                
                // Reward every minute (based on initial set time or elapsed)
                if (_timeLeft.value > 0 && 
                    (if (_timerMode.value == TimerMode.COUNT_UP) _timeLeft.value % 60 == 0 
                     else (getCurrentInitialTime() - _timeLeft.value) % 60 == 0)) {
                    recordFocus(1, _timerMode.value == TimerMode.POMODORO)
                }
            }
        }
    }

    private fun getCurrentInitialTime(): Int {
        return when(_timerMode.value) {
            TimerMode.POMODORO -> if (_isResting.value) prefs.pomodoroBreakMin * 60 else prefs.pomodoroFocusMin * 60
            TimerMode.COUNT_DOWN -> prefs.countdownTotalSeconds
            TimerMode.COUNT_UP -> 0
        }
    }

    private fun completeTimer() {
        if (_timerMode.value == TimerMode.POMODORO) {
            val totalRounds = prefs.pomodoroRounds
            if (_pomodoroPhase.value < totalRounds * 2 - 1) {
                recordFocus(0, true, isPhaseComplete = !_isResting.value)
                
                _pomodoroPhase.value++
                _isResting.value = _pomodoroPhase.value % 2 != 0
                _timeLeft.value = (if (_isResting.value) prefs.pomodoroBreakMin else prefs.pomodoroFocusMin) * 60
                pauseTimer()
            } else {
                recordFocus(0, true, isFullComplete = true)
                stopTimer()
            }
        } else {
            stopTimer()
        }
        saveTimerToPrefs()
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        saveTimerToPrefs()
    }

    fun stopTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        resetCurrentModeProgress()
        saveTimerToPrefs()
    }

    private fun resetCurrentModeProgress() {
        when (_timerMode.value) {
            TimerMode.POMODORO -> {
                _timeLeft.value = prefs.pomodoroFocusMin * 60
                _pomodoroPhase.value = 0
                _isResting.value = false
            }
            TimerMode.COUNT_UP -> _timeLeft.value = 0
            TimerMode.COUNT_DOWN -> _timeLeft.value = prefs.countdownTotalSeconds
        }
    }

    private fun saveTimerToPrefs() {
        prefs.timerMode = _timerMode.value.name
        prefs.isActiveSessionRunning = _isRunning.value
        prefs.lastActiveTimestamp = Clock.System.now().toEpochMilliseconds()
        
        when (_timerMode.value) {
            TimerMode.POMODORO -> {
                prefs.pomodoroCurrentSeconds = _timeLeft.value
                prefs.pomodoroCurrentPhase = _pomodoroPhase.value
                prefs.pomodoroIsResting = _isResting.value
            }
            TimerMode.COUNT_DOWN -> prefs.countdownCurrentSeconds = _timeLeft.value
            TimerMode.COUNT_UP -> prefs.countupCurrentSeconds = _timeLeft.value
        }
    }

    fun switchMode(newMode: TimerMode, behavior: TimerSwitchBehavior) {
        if (_timerMode.value == newMode) return

        pauseTimer()
        _timerMode.value = newMode

        _timeLeft.value = when (newMode) {
            TimerMode.POMODORO -> {
                _pomodoroPhase.value = prefs.pomodoroCurrentPhase
                _isResting.value = prefs.pomodoroIsResting
                prefs.pomodoroCurrentSeconds
            }
            TimerMode.COUNT_UP -> prefs.countupCurrentSeconds
            TimerMode.COUNT_DOWN -> prefs.countdownCurrentSeconds
        }
        
        saveTimerToPrefs()
    }

    fun updatePomodoroSettings(focus: Int, breakMin: Int, rounds: Int) {
        prefs.pomodoroFocusMin = focus
        prefs.pomodoroBreakMin = breakMin
        prefs.pomodoroRounds = rounds
        if (!_isRunning.value && _pomodoroPhase.value == 0 && !_isResting.value) {
            _timeLeft.value = focus * 60
            saveTimerToPrefs()
        }
    }

    fun updateCountDownSettings(seconds: Int) {
        prefs.countdownTotalSeconds = seconds
        if (!_isRunning.value) {
            _timeLeft.value = seconds
            saveTimerToPrefs()
        }
    }

    fun skipPhase() {
        if (_timerMode.value == TimerMode.POMODORO) {
            completeTimer()
        }
    }

    private suspend fun createDefaultUser() {
        val newUser = UserEntity(
            id = prefs.userId,
            username = prefs.userName,
            email = "",
            coins = 0,
            nutrient = 0,
            loginStreak = 1,
            totalFocusMinutes = 0,
            lastSyncAt = Clock.System.now().toEpochMilliseconds()
        )
        db.userDao().insertOrUpdateUser(newUser)
    }

    fun recordFocus(minutes: Int, isPomodoro: Boolean, isPhaseComplete: Boolean = false, isFullComplete: Boolean = false) {
        if (minutes <= 0 && !isPhaseComplete && !isFullComplete) return
        
        viewModelScope.launch {
            val userId = _user.value?.id ?: prefs.userId
            var coinsEarned = minutes

            if (isPomodoro) {
                if (isPhaseComplete && minutes >= 20) coinsEarned += 20
                if (isFullComplete) coinsEarned += 50
            }

            db.userDao().earnCoins(
                userId = userId,
                amount = coinsEarned,
                type = if (isPomodoro) "POMODORO" else "TIMER",
                description = "專注獎勵",
                refId = null,
                transactionDao = db.coinTransactionDao()
            )

            if (minutes > 0) {
                db.focusDao().insertSession(
                    FocusSessionEntity(
                        startTime = Clock.System.now().toEpochMilliseconds() - (minutes * 60000L),
                        duration = minutes,
                        isCompleted = true,
                        verifiedPassed = true
                    )
                )
            }

            _user.value?.let { currentUser ->
                db.userDao().insertOrUpdateUser(
                    currentUser.copy(
                        totalFocusMinutes = currentUser.totalFocusMinutes + minutes,
                        coins = currentUser.coins + coinsEarned
                    )
                )
            }
        }
    }
}
