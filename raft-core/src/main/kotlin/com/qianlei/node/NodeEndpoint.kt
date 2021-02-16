package com.qianlei.node

import com.qianlei.rpc.Address
import kotlinx.serialization.Serializable

/**
 * 节点信息
 * @author qianlei
 */
@Serializable
data class NodeEndpoint(
    /** 节点 ID */
    val id: NodeId,
    /** 节点地址信息 */
    val address: Address
) {
    constructor(id: String, address: Address) : this(NodeId(id), address)
    constructor(id: String, host: String, port: Int) : this(id, Address(host, port))
}