package com.qianlei.node

import com.qianlei.rpc.Address

data class NodeEndpoint(
    val id: NodeId,
    val address: Address
) {
    constructor(id: String, address: Address) : this(NodeId(id), address)

    constructor(id: String, host: String, port: Int) : this(id, Address(host, port))

}