package com.mary.myapplication.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

object ThreadUtil {
    // fixed number thread pool, can reuse
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    // only one thread can run at one time
    private val executorSequential = Executors.newSingleThreadExecutor()

    // sync UI
    private val handle = Handler(Looper.getMainLooper())


    fun startUIThread(delayMillis: Int, runnable: Runnable?) {
        handle.postDelayed(runnable!!, delayMillis.toLong())
    }

    fun startThread(runnable: Runnable?) {
        executor!!.submit(runnable)
    }

    fun startSingleThread(runnable: Runnable?) {
        executorSequential!!.submit(runnable)
    }
}