package com.qianlei.kv.message

import com.qianlei.node.NodeId
import kotlinx.serialization.Serializable

/**
 * 重定向响应
 *
 *
 * @author qianlei
 */
@Serializable
data class Redirect(val leaderId: String?) {
    constructor(leaderId: NodeId?) : this(leaderId?.value)
}
