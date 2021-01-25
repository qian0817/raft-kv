package com.qianlei.rpc

import com.qianlei.rpc.message.AppendEntriesResult
import com.qianlei.rpc.message.AppendEntriesRpc
import com.qianlei.rpc.message.RequestVoteResult
import com.qianlei.rpc.message.RequestVoteRpc
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
}