package com.qianlei.node

import kotlinx.serialization.Serializable

/**
 * 服务器 ID 类型，作为服务器成员的标识
 * 这里的NodeId只是字符串的一个封装，
 */
@Serializable
data class NodeId(val value: String) {
    companion object {
        fun of(value: String): NodeId {
            return NodeId(value)
        }
    }
}