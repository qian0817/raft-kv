package com.qianlei.log.sequence

import com.qianlei.log.entry.Entry
import mu.KotlinLogging
import java.util.*
import kotlin.math.max

/**
 * 日志条目序列有以下两个索引
 *
 * logIndexOffset 表示日志索引偏移
 * nextLogIndex 表示下一条日志的索引
 * 初始情况下 nextLogIndex = logIndexOffset
 *
 * @author qianlei
 */
abstract class AbstractEntrySequence(
    protected var logIndexOffset: Int
) : EntrySequence {
    private val logger = KotlinLogging.logger { }
    override var nextLogIndex = logIndexOffset

    override fun isEmpty() = logIndexOffset == nextLogIndex

    protected fun doGetFirstLogIndex() = logIndexOffset

    protected fun doGetLastLogIndex() = nextLogIndex - 1

    override val firstLogIndex: Int
        get() = if (isEmpty()) throw IllegalArgumentException("entrySequence is empty") else doGetFirstLogIndex()

    override val lastLogIndex: Int
        get() = if (isEmpty()) throw IllegalArgumentException("entrySequence is empty") else doGetLastLogIndex()

    override fun isEntryPresent(index: Int) =
        !isEmpty() && index >= doGetFirstLogIndex() && index <= doGetLastLogIndex()

    protected abstract fun doGetEntry(index: Int): Entry

    override fun getEntry(index: Int) = if (!isEntryPresent(index)) null else doGetEntry(index)

    override fun getEntryMeta(index: Int) = getEntry(index)?.meta

    override val lastEntry: Entry?
        get() = if (isEmpty()) null else doGetEntry(doGetLastLogIndex())

    override fun subList(fromIndex: Int) = subList(max(fromIndex, doGetFirstLogIndex()), nextLogIndex)

    override fun subList(fromIndex: Int, toIndex: Int): List<Entry> {
        if (isEmpty()) {
            return Collections.emptyList()
        }
        if (fromIndex < doGetFirstLogIndex() || toIndex > doGetLastLogIndex() + 1 || fromIndex > toIndex) {
            throw IllegalArgumentException("illegal from index $fromIndex  or to index $toIndex")
        }
        return doSubList(fromIndex, toIndex)
    }


    protected abstract fun doSubList(fromIndex: Int, toIndex: Int): List<Entry>

    override fun append(entries: List<Entry>) = entries.forEach { append(it) }

    override fun append(entry: Entry) {
        logger.debug { "append entry:$entry" }
        if (entry.index != nextLogIndex) {
            throw IllegalArgumentException("entry index must be $nextLogIndex")
        }
        doAppend(entry)
        nextLogIndex++
    }

    protected abstract fun doAppend(entry: Entry)

    override fun removeAfter(index: Int) {
        if (isEmpty() || index >= doGetLastLogIndex()) {
            return
        }
        doRemoveAfter(index)
    }

    protected abstract fun doRemoveAfter(index: Int)
}