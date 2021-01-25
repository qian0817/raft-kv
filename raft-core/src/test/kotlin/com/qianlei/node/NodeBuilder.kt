package com.qianlei.node

import com.google.common.eventbus.EventBus
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
class NodeBuilder {
    private val group: NodeGroup
    private val selfId: NodeId
    private val eventBus: EventBus
    private var connector: Connector? = null
    private var scheduler: Scheduler? = null
    private var taskExecutor: TaskExecutor? = null
    private var nodeStore: NodeStore? = null

    constructor(endpoint: NodeEndpoint) : this(Collections.singleton(endpoint), endpoint.id)

    constructor(endpoints: Collection<NodeEndpoint>, selfId: NodeId) {
        this.group = NodeGroup(endpoints, selfId)
        this.selfId = selfId
        this.eventBus = EventBus(selfId.value)
    }

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

    fun build(): NodeImpl = NodeImpl(buildContext())

    private fun buildContext(): NodeContext {
        val context = NodeContext(
            selfId,
            group,
            connector ?: MockConnector(),
            scheduler ?: NullSchedule(),
            eventBus,
            taskExecutor ?: DirectTaskExecutor(),
            MemoryNodeStore()
        )
        return context
    }

}