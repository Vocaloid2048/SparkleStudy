package com.voc2048.sparkle_study.types

enum class AvatarCategory {
    HAIR,       // 髮型
    CLOTHES,    // 服飾
    ACCESSORY,  // 配件 (眼鏡、背包、帽子等)
    BACKGROUND  // 角色背景
}

/**
 * 2D 虛擬角色的服飾與配件數據。
 */
data class AvatarItem(
    val id: String,
    val name: String,
    val category: AvatarCategory,
    val price: Int,               // 解鎖所需代幣 (全免課金)
    val isUnlocked: Boolean,      // 用戶是否已解鎖
    val assetPath: String,        // 2D 貼圖/資源路徑
    val isEquipped: Boolean       // 當前是否穿戴中
)
