package com.qianlei.node

import mu.KotlinLogging
import java.util.*

/**
 * 集群成员列表
 */
class NodeGroup {
    private val logger = KotlinLogging.logger { }

    /** 当前节点 ID */
    private val selfId: NodeId

    /** 成员表 */
    private var memberMap: Map<NodeId, GroupMember>

    /** 单节点构造函数 */
    constructor(point: NodeEndpoint) : this(Collections.singleton(point), point.id)

    /** 多节点构造函数 */
    constructor(endPoints: Collection<NodeEndpoint>, selfId: NodeId) {
        memberMap = buildMemberMap(endPoints)
        this.selfId = selfId
    }

    /**
     * 根据[endPoints]构造成员映射表
     * 返回的 Key 为节点 ID，Value 为成员信息
     */
    private fun buildMemberMap(endPoints: Collection<NodeEndpoint>): Map<NodeId, GroupMember> {
        require(endPoints.isNotEmpty()) { "endpoint is empty" }
        return endPoints.map { it.id to GroupMember(it) }.toMap()
    }

    /**
     * 按照[id]查找成员
     * @throws IllegalArgumentException 找不到对应的成员
     */
    fun findMember(id: NodeId): GroupMember {
        return findMemberOrNull(id) ?: throw IllegalArgumentException("没有node $id")
    }

    /**
     * 按照[id]查找成员
     * null 表示没有
     */
    fun findMemberOrNull(id: NodeId): GroupMember? = memberMap[id]

    /**
     * 除了自己以外的所有节点
     */
    fun listGroupMemberExceptSelf(): List<GroupMember> {
        return memberMap.values.filter { !it.idEquals(selfId) }.toList()
    }

    /**
     * 除了自己以外的所有节点端点信息
     */
    fun listEndpointExceptSelf(): List<NodeEndpoint> {
        return listGroupMemberExceptSelf().map { it.endpoint }
    }

    /**
     * 节点数量总和
     */
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

    data class NodeMatchIndex(
        val noeId: NodeId,
        val matchIndex: Int
    ) : Comparable<NodeMatchIndex> {

        override fun compareTo(other: NodeMatchIndex): Int {
            return matchIndex.compareTo(other.matchIndex)
        }

    }
}
