package com.qianlei.rpc

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val host: String,
    val port: Int
)