package com.qianlei.log.snapshot

import com.qianlei.rpc.message.InstallSnapshotRpc
import java.io.Closeable

/**
 *
 * @author qianlei
 */
interface SnapshotBuilder<T : Snapshot> : Closeable {
    /** 追加日志快照内容 */
    fun append(rpc: InstallSnapshotRpc)

    /** 导出日志快照 */
    fun build(): T
}