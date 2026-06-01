package com.voc2048.sparkle_study

import com.voc2048.sparkle_study.utils.UtilsTools
import kotlin.test.Test
import kotlin.test.assertEquals

class TimerLogicTest {

    /**
     * 測試目標：驗證秒數格式化為 HH:mm:ss 字串的正確性。
     * 執行方式：傳入 0、59、60、3600、90061 等不同秒數。
     * 預期結果：分別對應 "00:00:00"、"00:00:59"、"00:01:00"、"01:00:00"、"25:01:01"。
     */
    @Test
    fun testFormatSecondsToTimerString() {
        assertEquals("00:00:00", UtilsTools.formatSecondsToTimerString(0))
        assertEquals("00:00:59", UtilsTools.formatSecondsToTimerString(59))
        assertEquals("00:01:00", UtilsTools.formatSecondsToTimerString(60))
        assertEquals("01:00:00", UtilsTools.formatSecondsToTimerString(3600))
        assertEquals("25:01:01", UtilsTools.formatSecondsToTimerString(90061))
    }

    /**
     * 測試目標：驗證分鐘數格式化為閱讀友好字串 (h m)。
     * 執行方式：傳入 45、60、90 分鐘。
     * 預期結果：分別對應 "45m"、"1h 0m"、"1h 30m"。
     */
    @Test
    fun testFormatMinutesToTimeString() {
        assertEquals("45m", UtilsTools.formatMinutesToTimeString(45))
        assertEquals("1h 0m", UtilsTools.formatMinutesToTimeString(60))
        assertEquals("1h 30m", UtilsTools.formatMinutesToTimeString(90))
    }

    /**
     * 測試目標：驗證番茄鐘階段與輪數的數學邏輯。
     * 執行方式：傳入 Phase 0 到 2 進行計算。
     * 預期結果：偶數 Phase 為專注，奇數為休息；Phase 0, 1 屬於第 1 輪，Phase 2 進入第 2 輪。
     */
    @Test
    fun testPomodoroPhaseMath() {
        // Pomodoro logic: Even phases (0, 2, 4...) are Focus, Odd (1, 3, 5...) are Break
        fun isResting(phase: Int) = phase % 2 != 0
        fun getRoundNumber(phase: Int) = (phase / 2) + 1

        assertEquals(false, isResting(0), "Phase 0 should be Focus")
        assertEquals(true, isResting(1), "Phase 1 should be Break")
        assertEquals(1, getRoundNumber(0), "Phase 0 should be Round 1")
        assertEquals(1, getRoundNumber(1), "Phase 1 should be Round 1")
        assertEquals(2, getRoundNumber(2), "Phase 2 should be Round 2")
    }
}
