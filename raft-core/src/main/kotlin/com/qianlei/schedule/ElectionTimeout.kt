package com.qianlei.schedule

import mu.KotlinLogging
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class ElectionTimeout(private val scheduledFuture: ScheduledFuture<*>) {

    private val logger = KotlinLogging.logger { }

    companion object {
        val NONE: ElectionTimeout = ElectionTimeout(NullScheduledFuture())
    }

    fun cancel() {
        logger.debug("cancel election timeout")
        scheduledFuture.cancel(false)
    }

    override fun toString(): String {
        return when {
            scheduledFuture.isCancelled -> "ElectionTimeout(state=cancelled)"
            scheduledFuture.isDone -> "ElectionTimeout(state=done)"
            else -> "ElectionTimeout{delay=" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS).toString() + "ms}"
        }
    }
}