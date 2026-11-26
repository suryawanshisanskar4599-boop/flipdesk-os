package com.flipdesk.os

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class FlipDeskApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val prevHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                File(filesDir, "last_crash.txt").writeText(throwable.stackTraceToString())
            }
            prevHandler?.uncaughtException(thread, throwable)
        }
    }
}
