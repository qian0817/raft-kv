package com.qianlei.rpc

import kotlinx.serialization.Serializable

/**
 * 地址信息
 * @author qianlei
 */
@Serializable
data class Address(
    /** 主机 */
    val host: String,
    /** 端口 */
    val port: Int
)