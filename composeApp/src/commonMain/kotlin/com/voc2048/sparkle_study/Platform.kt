package com.voc2048.sparkle_study

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform