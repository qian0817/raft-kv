package com.qianlei.log.snapshot

import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.math.min

/**
 *
 * @author qianlei
 */
class MemorySnapshot(
    override val lastIncludeIndex: Int,
    override val lastIncludeTerm: Int,
    val data: ByteArray = ByteArray(0)
) : Snapshot {
    override val dataSize: Long = data.size.toLong()

    override fun readData(offset: Int, length: Int): SnapshotChunk {
        require(offset >= 0 && offset <= data.size) { "offset $offset out of bound" }
        val bufferLength = min(data.size - offset, length)
        val buffer = ByteArray(bufferLength)
        System.arraycopy(data, offset, buffer, 0, bufferLength)
        return SnapshotChunk(buffer, offset + length >= data.size)
    }

    override val dataStream: InputStream
        get() = ByteArrayInputStream(data)

    override fun close() {}

    override fun toString(): String {
        return "MemorySnapshot(lastIncludeIndex=$lastIncludeIndex, lastIncludeTerm=$lastIncludeTerm, dataSize=$dataSize)"
    }
}