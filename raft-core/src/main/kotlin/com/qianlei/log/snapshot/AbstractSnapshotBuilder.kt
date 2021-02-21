package com.qianlei.log.snapshot

import com.qianlei.rpc.message.InstallSnapshotRpc

/**
 *
 * @author qianlei
 */
abstract class AbstractSnapshotBuilder<T : Snapshot>(
    firstRpc: InstallSnapshotRpc
) : SnapshotBuilder<T> {
    protected val lastIncludedIndex: Int = firstRpc.lastIndex
    protected val lastIncludedTerm: Int = firstRpc.lastTerm
    private var offset: Int = firstRpc.data.size

    override fun append(rpc: InstallSnapshotRpc) {
        require(rpc.offset == offset) {
            "unexpected offset,excepted $offset , but was ${rpc.offset}"
        }
        require(rpc.lastIndex == lastIncludedIndex && rpc.lastTerm == lastIncludedTerm) {
            "unexpected last included index or term"
        }
        doWrite(rpc.data)
        offset += rpc.data.size
    }

    protected abstract fun doWrite(data: ByteArray)
}