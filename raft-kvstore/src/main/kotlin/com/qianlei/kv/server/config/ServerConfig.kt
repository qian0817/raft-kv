package com.qianlei.kv.server.config

import com.qianlei.node.NodeEndpoint
import com.qianlei.node.NodeId
import kotlinx.serialization.Serializable

@Serializable
data class ServerConfig(
    val port: Int,
    /**
     * 集群中所有结点的地址
     */
    val groupEndpoint: List<NodeEndpoint>,
    val selfId: NodeId,
    val dataPath: String = "data",
    /* 最小选举超时时间 */
    val minElectionTimeout: Int = 2000,
    /* 最大选举超时时间 */
    val maxElectionTimeout: Int = 4000,
    /* 初次日志复制延迟时间 */
    val logReplicationDelay: Int = 1000,
    /* 日志复制间隔 */
    val logReplicationInterval: Int = 200,
)
