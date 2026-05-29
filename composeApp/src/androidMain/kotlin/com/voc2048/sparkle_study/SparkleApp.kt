package com.voc2048.sparkle_study

import android.app.Application
import android.content.Context

class SparkleApp : Application() {
    companion object {
        lateinit var INSTANCE: SparkleApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}
