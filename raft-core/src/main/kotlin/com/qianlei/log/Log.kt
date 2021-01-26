package com.qianlei.log

import com.qianlei.log.entry.Entry
import com.qianlei.log.entry.EntryMeta
import com.qianlei.log.entry.GeneralEntry
import com.qianlei.log.entry.NoOpEntry
import com.qianlei.node.NodeId
import com.qianlei.rpc.message.AppendEntriesRpc

/**
 *
 * @author qianlei
 */
interface Log {
    companion object {
        const val ALL_ENTRIES = -1
    }

    /**
     * 最后一条日志的元信息
     */
    val lastEntryMeta: EntryMeta

    /**
     * 创建 AppendEntriesRpc 消息
     */
    fun createAppendEntriesRpc(
        term: Int,
        selfId: NodeId,
        nextIndex: Int,
        maxEntries: Int
    ): AppendEntriesRpc

    /**
     * 获取下一条日志的目录
     */
    val nextIndex: Int

    /**
     * 获取当前的 commitIndex
     */
    val commitIndex: Int

    /**
     * 判断对象的 lastLogIndex 以及 lastLogTerm 是否比自己新
     * 当收到 RequestVote 消息时，选择是否投票需要使用到该方法
     */
    fun isNewerThan(lastLogIndex: Int, lastLogTerm: Int): Boolean

    /**
     * 增加一个 NoOpEntry 日志
     */
    fun appendEntry(term: Int): NoOpEntry

    /**
     * 增加一个普通日志
     */
    fun appendEntry(term: Int, command: ByteArray): GeneralEntry

    /**
     * 追加来自 leader 的日志目录
     */
    fun appendEntriesFromLeader(prevLogIndex: Int, prevLogTerm: Int, leaderEntries: List<Entry>): Boolean

    /**
     * 推进 commitIndex
     */
    fun advanceCommitIndex(newCommitIndex: Int, currentTerm: Int)

//    fun setStateMachine(stateMachine:StateMachine)

    fun close()
}