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
    val requestId: String = UUID.randomUUID().toString(),
    val key: String,
    val value: ByteArray,
) {
    companion object {
        fun fromBytes(bytes: ByteArray): SetCommand {
            return ProtoBuf.decodeFromByteArray(bytes)
        }
    }

    fun toBytes(): ByteArray {
        return ProtoBuf.encodeToByteArray(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetCommand

        if (requestId != other.requestId) return false
        if (key != other.key) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = requestId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}
