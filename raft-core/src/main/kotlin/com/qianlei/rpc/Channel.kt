package com.qianlei.rpc

import com.qianlei.rpc.message.*
import java.io.Closeable


/**
 *
 * @author qianlei
 */
interface Channel : Closeable {

    /**
     * Write request vote rpc.
     *
     * @param rpc rpc
     */
    fun writeRequestVoteRpc(rpc: RequestVoteRpc)

    /**
     * Write request vote result.
     *
     * @param result result
     */
    fun writeRequestVoteResult(result: RequestVoteResult)

    /**
     * Write append entries rpc.
     *
     * @param rpc rpc
     */
    fun writeAppendEntriesRpc(rpc: AppendEntriesRpc)

    /**
     * Write append entries result.
     *
     * @param result result
     */
    fun writeAppendEntriesResult(result: AppendEntriesResult)

    fun writeInstallSnapshotResult(result: InstallSnapshotResult)

    fun writeInstallSnapshotRpc(rpc: InstallSnapshotRpc)
}