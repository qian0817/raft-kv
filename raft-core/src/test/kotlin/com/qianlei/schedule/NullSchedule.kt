package com.qianlei.schedule

import mu.KotlinLogging

/**
 *
 * @author qianlei
 */
class NullSchedule : Scheduler {
    private val logger = KotlinLogging.logger { }

    override fun scheduleLogReplicationTask(task: Runnable): LogReplicationTask {
        logger.debug { "schedule log replication task" }
        return LogReplicationTask.NONE
    }

    override fun scheduleElectionTimeout(task: Runnable): ElectionTimeout {
        logger.debug { "schedule election timeout" }
        return ElectionTimeout.NONE
    }

    override fun stop() {}
}