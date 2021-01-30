package com.qianlei.kv.message

import kotlinx.serialization.Serializable

/**
 *
 * Get 响应
 *
 * @author qianlei
 */
@Serializable
data class GetCommandResponse(
    val found: Boolean,
    val value: ByteArray?
) {
    constructor(value: ByteArray?) : this(value != null, value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GetCommandResponse

        if (found != other.found) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = found.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "GetCommandResponse(found=$found)"
    }
}