package com.voc2048.sparkle_study

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginStreakTest {

    /**
     * 測試目標：驗證連續登入時天數是否正確增加。
     * 執行方式：設定昨日 ("2023-10-24") 與今日 ("2023-10-25") 的日期字串並解析，計算日期差。
     * 預期結果：日期差應等於 1，且連續登入天數 (Streak) 從 5 增加至 6。
     */
    @Test
    fun testStreakIncrementsOnNextDay() {
        val lastLogin = "2023-10-24"
        val today = "2023-10-25"
        
        val lastLocalDate = LocalDate.parse(lastLogin)
        val todayDate = LocalDate.parse(today)
        val daysDiff = lastLocalDate.daysUntil(todayDate)
        
        assertEquals(1, daysDiff, "Days difference should be 1 for consecutive days")
        
        var streak = 5
        if (daysDiff == 1) {
            streak++
        }
        assertEquals(6, streak)
    }

    /**
     * 測試目標：驗證登入中斷時天數是否會重置。
     * 執行方式：設定一個超過 1 天的間隔 ("2023-10-20" 到 "2023-10-25")。
     * 預期結果：日期差大於 1，連續登入天數 (Streak) 應被重置為 1。
     */
    @Test
    fun testStreakResetsOnGap() {
        val lastLogin = "2023-10-20"
        val today = "2023-10-25"
        
        val lastLocalDate = LocalDate.parse(lastLogin)
        val todayDate = LocalDate.parse(today)
        val daysDiff = lastLocalDate.daysUntil(todayDate)
        
        var streak = 5
        if (daysDiff == 1) {
            streak++
        } else if (daysDiff > 1) {
            streak = 1
        }
        assertEquals(1, streak, "Streak should reset to 1 if more than one day passed")
    }
    
    /**
     * 測試目標：驗證 7 天獎勵週期的索引計算。
     * 執行方式：模擬 1 到 8 天的登入狀況，傳入計算函數獲取獎勵金額。
     * 預期結果：第 1 天應得 5 🪙，第 7 天應得最高 50 🪙，第 8 天應重回週期起點獲得 5 🪙。
     */
    @Test
    fun testStreakCycleRewardIndex() {
        fun getRewardAmount(streak: Int): Int {
            val rewards = listOf(5, 10, 5, 15, 5, 20, 50)
            val rewardIndex = (streak - 1) % 7
            return rewards[rewardIndex]
        }
        
        assertEquals(5, getRewardAmount(1))  // Day 1
        assertEquals(50, getRewardAmount(7)) // Day 7
        assertEquals(5, getRewardAmount(8))  // Day 8 (Cycle starts over)
    }
}
