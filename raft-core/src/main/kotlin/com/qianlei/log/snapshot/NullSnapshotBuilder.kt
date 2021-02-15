package com.qianlei.log.snapshot

import com.qianlei.rpc.message.InstallSnapshotRpc

/**
 *
 * @author qianlei
 */
class NullSnapshotBuilder : SnapshotBuilder<Snapshot> {
    override fun append(rpc: InstallSnapshotRpc) {
        throw UnsupportedOperationException()
    }

    override fun build(): Snapshot {
        throw UnsupportedOperationException()
    }

    override fun close() {}
}