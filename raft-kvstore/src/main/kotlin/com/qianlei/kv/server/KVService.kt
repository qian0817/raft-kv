package com.qianlei.kv.server

import com.qianlei.node.role.RoleNameAndLeaderId
import io.netty.channel.ChannelHandlerContext

/**
 *
 * @author qianlei
 */
interface KVService {
    fun getAll(): Map<String, String>

    fun get(key: String): String?

    fun set(key: String, value: String, ctx: ChannelHandlerContext)

    fun getNodeState(): RoleNameAndLeaderId
}