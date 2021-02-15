package com.qianlei.log.snapshot

import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * @author qianlei
 */
class EmptySnapshot : Snapshot {
    override val lastIncludeIndex = 0
    override val lastIncludeTerm = 0
    override val dataSize = 0L

    override fun readData(offset: Int, length: Int): SnapshotChunk {
        require(offset == 0) { "offset must > 0" }
        return SnapshotChunk(ByteArray(0), true)
    }

    override val dataStream: InputStream
        get() = ByteArrayInputStream(ByteArray(0))

    override fun close() {}
}