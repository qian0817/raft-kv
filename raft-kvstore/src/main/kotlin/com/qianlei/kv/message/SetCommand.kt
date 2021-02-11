package com.qianlei.kv.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.util.*

/**
 * SET 请求
 *
 * @author qianlei
 */
@Suppress("EXPERIMENTAL_API_USAGE")
@Serializable
data class SetCommand(
    val key: String,
    val value: String,
    val requestId: String = UUID.randomUUID().toString(),
) {
    companion object {
        fun fromBytes(bytes: ByteArray): SetCommand {
            return ProtoBuf.decodeFromByteArray(bytes)
        }
    }

    fun toBytes(): ByteArray {
        return ProtoBuf.encodeToByteArray(this)
    }
}
