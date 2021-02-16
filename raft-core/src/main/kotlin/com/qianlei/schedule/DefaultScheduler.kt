package com.qianlei.schedule

import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * 默认计时器的实现
 * @author qianlei
 */
class DefaultScheduler(
    /* 最小选举超时时间 */
    private val minElectionTimeout: Long,
    /* 最大选举超时时间 */
    private val maxElectionTimeout: Long,
    /* 初次日志复制延迟时间 */
    private val logReplicationDelay: Long,
    /* 日志复制间隔 */
    private val logReplicationInterval: Long
) : Scheduler {
    private val logger = KotlinLogging.logger { }
    private val scheduleExecutorService: ScheduledExecutorService

    init {
        // 判断参数是否有效
        require(minElectionTimeout > 0 && maxElectionTimeout > 0 && minElectionTimeout <= maxElectionTimeout) {
            "election timeout should > 0 and max > min "
        }
        require(logReplicationInterval >= 0 && logReplicationDelay >= 0) {
            "log replication delay <0 or log replication interval <=0"
        }
        scheduleExecutorService = Executors.newSingleThreadScheduledExecutor { Thread(it, "scheduler") }
    }

    /**
     * 负责心跳消息的日志复制定时器
     * 是固定间隔执行的定时器
     *
     */
    override fun scheduleLogReplicationTask(task: Runnable): LogReplicationTask {
        val scheduledFuture = scheduleExecutorService.scheduleWithFixedDelay(
            task, logReplicationDelay, logReplicationInterval, TimeUnit.MILLISECONDS
        )
        return LogReplicationTask(scheduledFuture)
    }

    /**
     * 创建选举超时计时器
     * Raft 算法为了减少 split vote 的影响。要求在超时区间内随机选择一个时间而不是固定的选举超时时间。
     */
    override fun scheduleElectionTimeout(task: Runnable): ElectionTimeout {
        // 随机超时时间
        val timeOut = Random.nextLong(minElectionTimeout, maxElectionTimeout)
        logger.trace { "set timeout $timeOut" }
        val scheduledFuture = scheduleExecutorService.schedule(task, timeOut, TimeUnit.MILLISECONDS)
        return ElectionTimeout(scheduledFuture)
    }

    override fun stop() {
        scheduleExecutorService.shutdown()
    }
}