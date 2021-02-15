package com.qianlei.log.snapshot

/**
 *
 * @author qianlei
 */
data class SnapshotChunk(
    val data: ByteArray,
    val lastChunk: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SnapshotChunk

        if (!data.contentEquals(other.data)) return false
        if (lastChunk != other.lastChunk) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + lastChunk.hashCode()
        return result
    }
}