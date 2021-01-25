package com.qianlei.schedule

import mu.KotlinLogging
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class LogReplicationTask(private val scheduledFuture: ScheduledFuture<*>) {
    private val logger = KotlinLogging.logger { }

    companion object {
        val NONE = LogReplicationTask(NullScheduledFuture())
    }

    fun cancel() {
        logger.debug("cancel log replication task")
        scheduledFuture.cancel(false)
    }

    override fun toString(): String {
        return "LogReplicationTask{delay=" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + "}"
    }
}