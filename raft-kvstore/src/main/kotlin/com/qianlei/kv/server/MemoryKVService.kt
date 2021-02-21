package com.qianlei.kv.server

import com.qianlei.node.NodeId
import com.qianlei.node.role.RoleName
import com.qianlei.node.role.RoleNameAndLeaderId
import io.netty.channel.ChannelHandlerContext

/**
 *
 * @author qianlei
 */
class MemoryKVService : KVService {
    private val map = hashMapOf<String, String>()
    override fun getAll(): Map<String, String> {
        return map
    }

    override fun get(key: String): String? {
        return map[key]
    }

    override fun set(key: String, value: String, ctx: ChannelHandlerContext) {
        map[key] = value
    }

    override fun getNodeState(): RoleNameAndLeaderId {
        return RoleNameAndLeaderId(RoleName.LEADER, NodeId.of("A"))
    }
}