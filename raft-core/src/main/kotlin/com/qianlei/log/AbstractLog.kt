package com.qianlei.log

import com.qianlei.log.Log.Companion.ALL_ENTRIES
import com.qianlei.log.entry.Entry
import com.qianlei.log.entry.EntryMeta
import com.qianlei.log.entry.GeneralEntry
import com.qianlei.log.entry.NoOpEntry
import com.qianlei.log.sequence.EntrySequence
import com.qianlei.log.statemachine.StateMachine
import com.qianlei.node.NodeId
import com.qianlei.rpc.message.AppendEntriesRpc
import mu.KotlinLogging
import kotlin.math.min

/**
 *
 * @author qianlei
 */
abstract class AbstractLog : Log {
    protected lateinit var entrySequence: EntrySequence
    private val logger = KotlinLogging.logger { }

    override val nextIndex
        get() = entrySequence.nextLogIndex
    override val commitIndex: Int
        get() = entrySequence.commitIndex

    override lateinit var stateMachine: StateMachine

    /**
     * 获取最后一条日志的元信息
     */
    override val lastEntryMeta: EntryMeta
        get() = if (entrySequence.isEmpty()) {
            EntryMeta(Entry.KIND_NO_OP, 0, 0)
        } else {
            entrySequence.lastEntry!!.meta
        }

    /**
     * 创建 AppendEntries 消息
     */
    override fun createAppendEntriesRpc(term: Int, selfId: NodeId, nextIndex: Int, maxEntries: Int): AppendEntriesRpc {
        val nextLogIndex = entrySequence.nextLogIndex
        require(nextIndex <= nextLogIndex) { "illegal next index $nextIndex" }
        // 设置前一条日志的元信息
        val entry = entrySequence.getEntry(nextIndex - 1)
        val entries = if (!entrySequence.isEmpty()) {
            val maxIndex = if (maxEntries == ALL_ENTRIES) nextLogIndex else min(nextLogIndex, nextIndex + maxEntries)
            entrySequence.subList(nextIndex, maxIndex)
        } else {
            emptyList()
        }
        val prevLogIndex = entry?.index ?: 0
        val prevLogTerm = entry?.term ?: 0
        return AppendEntriesRpc(term, selfId, prevLogIndex, prevLogTerm, entries, commitIndex)
    }

    override fun isNewerThan(lastLogIndex: Int, lastLogTerm: Int): Boolean {
        logger.debug {
            "last entry (${lastEntryMeta.index}, ${lastEntryMeta.term})," +
                    " candidate ($lastLogIndex, $lastLogTerm)"
        }
        return lastEntryMeta.term > lastLogTerm || lastEntryMeta.index > lastLogIndex
    }

    override fun appendEntry(term: Int): NoOpEntry {
        val entry = NoOpEntry(entrySequence.nextLogIndex, term)
        entrySequence.append(entry)
        return entry
    }

    override fun appendEntry(term: Int, command: ByteArray): GeneralEntry {
        val entry = GeneralEntry(entrySequence.nextLogIndex, term, command)
        entrySequence.append(entry)
        return entry
    }

    /**
     * 追加日志条目
     * 在追加前需要移除不一致的日志条目。
     * 需要先检查从 Leader 节点过来的 prevLogIndex 以及 prevLogTerm 是否匹配本地日志，如果不匹配直接返回 false
     * 移除时从最后一条匹配的日志条目开始，之后所有冲突的日志都会被移除
     *
     */
    override fun appendEntriesFromLeader(prevLogIndex: Int, prevLogTerm: Int, leaderEntries: List<Entry>): Boolean {
        logger.debug { "prevLogIndex: $prevLogIndex prevLogTerm:$prevLogTerm" }
        if (!checkIfPreviousLogMatches(prevLogIndex, prevLogTerm)) {
            return false
        }
        if (leaderEntries.isEmpty()) {
            return true
        }
        val newEntries = removeUnmatchedLog(EntrySequenceView(leaderEntries))
        appendEntriesFromLeader(newEntries)
        return true
    }

    /**
     * 追加日志条目
     */
    private fun appendEntriesFromLeader(leaderEntries: EntrySequenceView) {
        if (leaderEntries.isEmpty()) {
            return
        }
        logger.debug {
            "append entries from leader " +
                    "from ${leaderEntries.firstLogIndex} " +
                    "to ${leaderEntries.lastLogIndex}"
        }
        leaderEntries.forEach(entrySequence::append)
    }

    /**
     * 移除不一致的日志条目
     * 会先找到第一个不一致的日志条目，移除之后所有的日志条目
     */
    private fun removeUnmatchedLog(leaderEntries: EntrySequenceView): EntrySequenceView {
        val firstUnmatched = findFirstUnmatchedLog(leaderEntries) ?: return EntrySequenceView(emptyList())
        removeEntriesAfter(firstUnmatched - 1)
        return leaderEntries.subView(firstUnmatched)
    }

    /**
     * 移除[index]之后的所有日志
     * 如果本地日志为空或者对应最后一条日志的索引则不进行任何操作
     */
    private fun removeEntriesAfter(index: Int) {
        if (entrySequence.isEmpty() || index >= entrySequence.lastLogIndex) {
            return
        }
        logger.debug { "remove entries after $index" }
        entrySequence.removeAfter(index)
    }

    /**
     * 查找第一条不匹配的日志
     * 没有不匹配的日志则返回 null
     */
    private fun findFirstUnmatchedLog(leaderEntries: EntrySequenceView): Int? {
        return leaderEntries.find {
            val followerEntryMeta = entrySequence.getEntryMeta(it.index)
            followerEntryMeta == null || followerEntryMeta.term != it.term
        }?.index
    }

    private fun checkIfPreviousLogMatches(prevLogIndex: Int, prevLogTerm: Int): Boolean {
        // 检查是否存在指定索引的日志条目
        val meta = entrySequence.getEntryMeta(prevLogIndex)
        if (meta == null) {
            logger.debug { "previous log $prevLogIndex not found" }
            return false
        }
        val term = meta.term
        if (term != prevLogTerm) {
            logger.debug { "different term of previous log,local $term ,remote $prevLogTerm" }
            return false
        }
        return true
    }

    /**
     * 推进 commitIndex
     */
    override fun advanceCommitIndex(newCommitIndex: Int, currentTerm: Int) {
        if (!validateNewCommitIndex(newCommitIndex, currentTerm)) {
            return
        }
        logger.debug { "advance commit index from $commitIndex to $newCommitIndex" }
        entrySequence.commit(newCommitIndex)
        advanceApplyIndex()
    }

    private fun advanceApplyIndex() {
        // start up and snapshot exists
        val lastApplied = stateMachine.getLastApplied()
        for (entry in entrySequence.subList(lastApplied + 1, commitIndex + 1)) {
            applyEntry(entry)
        }
    }

    private fun applyEntry(entry: Entry) {
        if (entry.kind == Entry.KIND_GENERAL) {
            stateMachine.applyLog(entry.index, entry.commandBytes)
        }
    }

    /**
     * 检查新的 commitIndex
     */
    private fun validateNewCommitIndex(newCommitIndex: Int, currentTerm: Int): Boolean {
        if (newCommitIndex <= entrySequence.commitIndex) {
            return false
        }
        val meta = entrySequence.getEntryMeta(newCommitIndex)
        if (meta == null) {
            logger.debug { "log of new commit index $newCommitIndex not found" }
            return false
        }
        if (meta.term != currentTerm) {
            logger.debug { "log term of new commit index != current term ${meta.term} != $currentTerm" }
            return false
        }
        return true
    }

    /**
     * 作为 Entry 数组的封装，提供按照日志索引，根据子视图检查的功能
     */
    private class EntrySequenceView(private val entries: List<Entry>) : Iterable<Entry> {
        var firstLogIndex = 0
        var lastLogIndex = 0

        init {
            if (entries.isNotEmpty()) {
                firstLogIndex = entries.first().index
                lastLogIndex = entries.last().index
            }
        }

        operator fun get(index: Int): Entry? {
            if (entries.isEmpty() || index < firstLogIndex || index > lastLogIndex) {
                return null
            }
            return entries[index - firstLogIndex]
        }

        fun isEmpty(): Boolean {
            return entries.isEmpty()
        }

        fun subView(fromIndex: Int): EntrySequenceView = if (entries.isEmpty() || fromIndex > lastLogIndex) {
            EntrySequenceView(emptyList())
        } else {
            EntrySequenceView(entries.subList(fromIndex - firstLogIndex, entries.size))
        }

        override fun iterator(): Iterator<Entry> = entries.iterator()
    }

    override fun close() {
        entrySequence.close()
        stateMachine.shutdown()
    }
}