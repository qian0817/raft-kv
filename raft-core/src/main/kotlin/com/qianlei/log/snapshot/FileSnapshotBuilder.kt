package com.qianlei.log.snapshot

import com.qianlei.log.LogDir
import com.qianlei.rpc.message.InstallSnapshotRpc

/**
 *
 * @author qianlei
 */
class FileSnapshotBuilder(
    firstRpc: InstallSnapshotRpc,
    private val logDir: LogDir
) : AbstractSnapshotBuilder<FileSnapshot>(firstRpc) {
    private val writer =
        FileSnapshotWriter(logDir.getSnapshotFile(), firstRpc.lastIndex, firstRpc.lastTerm)
    private var closed = false

    init {
        writer.write(firstRpc.data)
    }

    override fun doWrite(data: ByteArray) {
        writer.write(data)
    }

    override fun build(): FileSnapshot {
        close()
        return FileSnapshot(logDir)
    }

    override fun close() {
        if (closed) {
            return
        }
        writer.close()
        closed = true
    }
}