package com.qianlei.node

import com.qianlei.rpc.Address
import kotlinx.serialization.Serializable

@Serializable
data class NodeEndpoint(
    val id: NodeId,
    val address: Address
) {
    constructor(id: String, address: Address) : this(NodeId(id), address)

    constructor(id: String, host: String, port: Int) : this(id, Address(host, port))

}