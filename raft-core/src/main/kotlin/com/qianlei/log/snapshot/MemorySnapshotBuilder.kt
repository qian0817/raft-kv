package com.qianlei.log.snapshot

import com.qianlei.rpc.message.InstallSnapshotRpc
import java.io.ByteArrayOutputStream

/**
 *
 * @author qianlei
 */
class MemorySnapshotBuilder(
    firstRpc: InstallSnapshotRpc
) : AbstractSnapshotBuilder<MemorySnapshot>(firstRpc) {
    private val output = ByteArrayOutputStream()

    init {
        output.write(firstRpc.data)
    }

    override fun doWrite(data: ByteArray) {
        output.write(data)
    }

    override fun build(): MemorySnapshot {
        return MemorySnapshot(lastIncludedIndex, lastIncludedTerm, output.toByteArray())
    }

    override fun close() {
    }
}