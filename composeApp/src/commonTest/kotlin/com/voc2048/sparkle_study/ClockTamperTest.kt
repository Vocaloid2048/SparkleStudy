package com.voc2048.sparkle_study

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClockTamperTest {

    /**
     * 模擬 DashboardViewModel 中的時鐘篡改偵測邏輯。
     * 核心邏輯：比對「系統時間差 (System Delta)」與「硬體啟動時間差 (Uptime Delta)」。
     */
    private fun isClockTampered(
        lastSystemTime: Long,
        lastUptime: Long,
        currentSystemTime: Long,
        currentUptime: Long
    ): Boolean {
        if (lastSystemTime == 0L || lastUptime == 0L) return false
        
        val systemDelta = currentSystemTime - lastSystemTime
        val uptimeDelta = currentUptime - lastUptime
        
        // Only check if device hasn't rebooted
        if (currentUptime > lastUptime) {
            // Tamper detected if:
            // 1. System time went backwards (systemDelta < 0)
            // 2. System time jumped forward much faster than uptime (e.g., > 10 mins difference)
            if (systemDelta < -600000 || (systemDelta - uptimeDelta) > 600000) {
                return true
            }
        }
        return false
    }

    /**
     * 測試目標：驗證正常時間流逝是否會被誤判。
     * 執行方式：設定系統時間與 Uptime 同步增加 60 秒 (1分鐘)。
     * 預期結果：判定為非篡改 (false)。
     */
    @Test
    fun testNormalTimeProgression() {
        val lastSys = 1000000L
        val lastUp = 50000L
        
        // 1 minute passes
        val currSys = lastSys + 60000L
        val currUp = lastUp + 60000L
        
        assertFalse(isClockTampered(lastSys, lastUp, currSys, currUp), "Should not detect tamper for normal progression")
    }

    /**
     * 測試目標：偵測使用者將系統時間「調回過去」的行為。
     * 執行方式：設定系統時間減少 1 小時，但 Uptime 正常增加 1 秒。
     * 預期結果：判定為篡改 (true)。
     */
    @Test
    fun testClockBackwardsDetected() {
        val lastSys = 1000000L
        val lastUp = 50000L
        
        // User sets clock back by 1 hour
        val currSys = lastSys - 3600000L
        val currUp = lastUp + 1000L // 1 second actual uptime passed
        
        assertTrue(isClockTampered(lastSys, lastUp, currSys, currUp), "Should detect backwards clock change")
    }

    /**
     * 測試目標：偵測使用者將系統時間「大幅調快」的行為。
     * 執行方式：設定系統時間增加 1 天，但 Uptime 僅實際經過 1 秒。
     * 預期結果：判定為篡改 (true)。
     */
    @Test
    fun testClockForwardJumpDetected() {
        val lastSys = 1000000L
        val lastUp = 50000L
        
        // User sets clock forward by 1 day
        val currSys = lastSys + 86400000L
        val currUp = lastUp + 1000L // 1 second actual uptime passed
        
        assertTrue(isClockTampered(lastSys, lastUp, currSys, currUp), "Should detect large forward jump")
    }
    
    /**
     * 測試目標：驗證系統自動對時產生的微小漂移是否被容許。
     * 執行方式：設定系統時間增加 62 秒，而 Uptime 增加 60 秒（存在 2 秒誤差）。
     * 預期結果：在 10 分鐘誤差容許範圍內，判定為非篡改 (false)。
     */
    @Test
    fun testSmallSystemDriftAllowed() {
        val lastSys = 1000000L
        val lastUp = 50000L
        
        // 1 minute passes, but system clock syncs and moves slightly (e.g., 2 seconds drift)
        val currSys = lastSys + 62000L
        val currUp = lastUp + 60000L
        
        assertFalse(isClockTampered(lastSys, lastUp, currSys, currUp), "Small drift should be allowed")
    }
}
