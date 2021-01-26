package com.qianlei.node

import com.google.common.eventbus.EventBus
import com.qianlei.log.Log
import com.qianlei.node.store.NodeStore
import com.qianlei.rpc.Connector
import com.qianlei.schedule.Scheduler
import com.qianlei.support.TaskExecutor

@Suppress("UnstableApiUsage")
data class NodeContext(
    val selfId: NodeId,
    val group: NodeGroup,
    val log: Log,
    val connector: Connector,
    val scheduler: Scheduler,
    val eventBus: EventBus,
    val taskExecutor: TaskExecutor,
    val store: NodeStore
)
