package com.qianlei.schedule


import java.util.concurrent.Delayed
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class NullScheduledFuture : ScheduledFuture<Any?> {
    override fun getDelay(unit: TimeUnit): Long {
        return 0
    }

    override operator fun compareTo(other: Delayed): Int {
        return 0
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return false
    }

    override fun isCancelled(): Boolean {
        return false
    }

    override fun isDone(): Boolean {
        return false
    }

    override fun get(): Any? {
        return null
    }

    override fun get(timeout: Long, unit: TimeUnit): Any? {
        return null
    }
}