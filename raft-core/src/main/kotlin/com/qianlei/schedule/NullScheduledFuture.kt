package com.qianlei.schedule


import java.util.concurrent.Delayed
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class NullScheduledFuture : ScheduledFuture<Any?> {
    override fun getDelay(unit: TimeUnit): Long = 0

    override operator fun compareTo(other: Delayed): Int = 0

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false

    override fun isCancelled(): Boolean = false

    override fun isDone(): Boolean = false

    override fun get(): Any? = null

    override fun get(timeout: Long, unit: TimeUnit): Any? = null
}