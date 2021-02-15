package com.qianlei.node

import mu.KotlinLogging
import java.util.*

/**
 * 集群成员
 */
class NodeGroup {
    private val logger = KotlinLogging.logger { }

    /**
     * 当前节点 ID
     */
    private val selfId: NodeId

    /**
     * 成员表
     */
    private var memberMap: Map<NodeId, GroupMember>

    /**
     * 单节点构造函数
     */
    constructor(point: NodeEndpoint) : this(Collections.singleton(point), point.id)

    /**
     * 多节点构造函数
     */
    constructor(endPoints: Collection<NodeEndpoint>, selfId: NodeId) {
        memberMap = buildMemberMap(endPoints)
        this.selfId = selfId
    }

    /**
     * 从节点列表中构造成员映射表
     */
    private fun buildMemberMap(endPoints: Collection<NodeEndpoint>): Map<NodeId, GroupMember> {
        if (endPoints.isEmpty()) {
            throw IllegalArgumentException("endpoint is empty")
        }
        return endPoints.map { it.id to GroupMember(it) }.toMap()
    }

    fun findMember(id: NodeId): GroupMember {
        return memberMap[id] ?: throw IllegalArgumentException("没有node $id")
    }

    fun getMember(id: NodeId): GroupMember? = memberMap[id]

    /**
     * 日志复制的对象节点
     * 也就是除了自己以外的所有节点
     */
    fun listReplicationTarget(): List<GroupMember> {
        return memberMap.values.filter { !it.idEquals(selfId) }.toList()
    }

    fun listEndpointExceptSelf(): List<NodeEndpoint> {
        return memberMap.values
            .asSequence()
            .map { it.endpoint }
            .filter { it.id != selfId }
            .toList()
    }

    fun count() = memberMap.size

    fun getMatchIndexOfMajor(): Int {

        val matchIndices = memberMap.values
            .filter { !it.idEquals(selfId) }
            .map { NodeMatchIndex(it.endpoint.id, it.getMatchIndex()) }
            .sorted()
            .toList()
        check(matchIndices.isNotEmpty()) { "standalone or no major node" }
        logger.debug("match indices {}", matchIndices)
        return matchIndices[matchIndices.size / 2].matchIndex
    }

    fun resetReplicatingStates(nextIndex: Int) {
        for (member in memberMap.values) {
            if (!member.idEquals(selfId)) {
                member.replicatingState = ReplicatingState(nextIndex)
            }
        }
    }

    //    fun updateNodes(endpoints: List<NodeEndpoint>) {
//        memberMap = buildMemberMap(endpoints)
//        logger.info { "group change changed -> ${memberMap.keys}" }
//    }
//
    data class NodeMatchIndex(
        val noeId: NodeId,
        val matchIndex: Int
    ) : Comparable<NodeMatchIndex> {

        override fun compareTo(other: NodeMatchIndex): Int {
            return matchIndex.compareTo(other.matchIndex)
        }

    }
}
