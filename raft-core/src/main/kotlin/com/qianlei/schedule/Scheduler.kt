package com.qianlei.schedule

/**
 * 计时器接口
 * @author qianlei
 */
interface Scheduler {
    /** 创建日志复制定时任务 */
    fun scheduleLogReplicationTask(task: Runnable): LogReplicationTask

    /** 创建选举超时器 */
    fun scheduleElectionTimeout(task: Runnable): ElectionTimeout

    /** 关闭计时器 */
    fun stop()
}