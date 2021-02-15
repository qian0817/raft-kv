package com.qianlei.node

import com.google.common.eventbus.EventBus
import com.qianlei.log.Log
import com.qianlei.log.MemoryLog
import com.qianlei.log.sequence.MemoryEntrySequence
import com.qianlei.node.store.MemoryNodeStore
import com.qianlei.node.store.NodeStore
import com.qianlei.rpc.Connector
import com.qianlei.rpc.MockConnector
import com.qianlei.schedule.NullSchedule
import com.qianlei.schedule.Scheduler
import com.qianlei.support.DirectTaskExecutor
import com.qianlei.support.TaskExecutor
import java.util.*


/**
 *
 * @author qianlei
 */
@Suppress("UnstableApiUsage")
class NodeBuilder(
    endpoints: Collection<NodeEndpoint>,
    private val selfId: NodeId
) {
    private val group: NodeGroup = NodeGroup(endpoints, selfId)
    private val eventBus: EventBus = EventBus(selfId.value)
    private var log: Log = MemoryLog(MemoryEntrySequence(), eventBus)
    private var connector: Connector = MockConnector()
    private var scheduler: Scheduler = NullSchedule()
    private var taskExecutor: TaskExecutor = DirectTaskExecutor()
    private var nodeStore: NodeStore = MemoryNodeStore()

    constructor(endpoint: NodeEndpoint) : this(Collections.singleton(endpoint), endpoint.id)

    fun setConnector(connector: Connector): NodeBuilder {
        this.connector = connector
        return this
    }

    fun setScheduler(scheduler: Scheduler): NodeBuilder {
        this.scheduler = scheduler
        return this
    }

    fun setTaskExecutor(taskExecutor: TaskExecutor): NodeBuilder {
        this.taskExecutor = taskExecutor
        return this
    }

    fun setStore(nodeStore: NodeStore): NodeBuilder {
        this.nodeStore = nodeStore
        return this
    }

    fun setLog(log: Log): NodeBuilder {
        this.log = log
        return this
    }

    fun build(): NodeImpl = NodeImpl(buildContext())

    private fun buildContext(): NodeContext {
        return NodeContext(
            selfId,
            group,
            log,
            connector,
            scheduler,
            eventBus,
            taskExecutor,
            nodeStore
        )
    }

}