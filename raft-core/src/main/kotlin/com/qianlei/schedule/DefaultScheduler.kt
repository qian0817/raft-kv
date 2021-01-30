package com.qianlei.schedule

import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * 默认计时器的实现
 */
class DefaultScheduler(
    /* 最小选举超时时间 */
    private val minElectionTimeout: Int,
    /* 最大选举超时时间 */
    private val maxElectionTimeout: Int,
    /* 初次日志复制延迟时间 */
    private val logReplicationDelay: Int,
    /* 日志复制间隔 */
    private val logReplicationInterval: Int
) : Scheduler {
    private val logger = KotlinLogging.logger { }
    private val scheduleExecutorService: ScheduledExecutorService

    init {
        // 判断参数是否有效
        if (minElectionTimeout <= 0 || maxElectionTimeout <= 0 || minElectionTimeout > maxElectionTimeout) {
            throw IllegalArgumentException("选举超时时间必须大于0并且 minElectionTimeout 需要小于 maxElectionTimeout")
        }
        if (logReplicationDelay < 0 || logReplicationInterval <= 0) {
            throw IllegalArgumentException("初次日志复制延时小于0或复制间隔小于0")
        }
        scheduleExecutorService = Executors.newSingleThreadScheduledExecutor { Thread(it, "scheduler") }
    }

    override fun scheduleLogReplicationTask(task: Runnable): LogReplicationTask {
        val scheduledFuture = scheduleExecutorService.scheduleWithFixedDelay(
            task,
            logReplicationDelay.toLong(),
            logReplicationInterval.toLong(),
            TimeUnit.MILLISECONDS
        )
        return LogReplicationTask(scheduledFuture)
    }

    override fun scheduleElectionTimeout(task: Runnable): ElectionTimeout {
        // 随机超时时间
        val timeOut = Random.nextInt(maxElectionTimeout - minElectionTimeout) + minElectionTimeout
        logger.trace { "set timeout $timeOut" }
        val scheduledFuture = scheduleExecutorService.schedule(
            task,
            timeOut.toLong(),
            TimeUnit.MILLISECONDS
        )
        return ElectionTimeout(scheduledFuture)
    }

    override fun stop() {
        scheduleExecutorService.shutdown()
    }
}