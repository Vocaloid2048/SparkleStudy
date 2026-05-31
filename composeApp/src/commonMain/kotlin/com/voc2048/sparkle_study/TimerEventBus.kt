package com.voc2048.sparkle_study

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class TimerEvent {
    PAUSE, RESUME, SKIP
}

object TimerEventBus {
    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun post(event: TimerEvent) {
        _events.tryEmit(event)
    }
}
