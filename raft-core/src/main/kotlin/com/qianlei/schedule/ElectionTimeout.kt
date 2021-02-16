package com.qianlei.schedule

import mu.KotlinLogging
import java.util.concurrent.ScheduledFuture

/**
 * 选举超时
 * @author qianlei
 */
class ElectionTimeout(private val scheduledFuture: ScheduledFuture<*>) {
    private val logger = KotlinLogging.logger { }

    companion object {
        val NONE: ElectionTimeout = ElectionTimeout(NullScheduledFuture())
    }

    fun cancel() {
        logger.debug("cancel election timeout")
        scheduledFuture.cancel(false)
    }
}