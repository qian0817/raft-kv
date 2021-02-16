package com.qianlei.schedule

import mu.KotlinLogging
import java.util.concurrent.ScheduledFuture

/**
 * 日志复制任务
 * @author qianlei
 */
class LogReplicationTask(private val scheduledFuture: ScheduledFuture<*>) {
    private val logger = KotlinLogging.logger { }

    companion object {
        val NONE = LogReplicationTask(NullScheduledFuture())
    }

    fun cancel() {
        logger.debug("cancel log replication task")
        scheduledFuture.cancel(false)
    }
}