package com.qianlei.log.sequence

import com.qianlei.log.entry.Entry

/**
 * 基于内存的日志序列实现，使用一个Entry列表作为存储对象
 * @author qianlei
 */
class MemoryEntrySequence(logIndexOffset: Int = 1) : AbstractEntrySequence(logIndexOffset) {
    private val entries = arrayListOf<Entry>()
    override fun doGetEntry(index: Int) = entries[index - logIndexOffset]

    override fun doSubList(fromIndex: Int, toIndex: Int) =
        entries.subList(fromIndex - logIndexOffset, toIndex - logIndexOffset)

    override fun doAppend(entry: Entry) {
        entries.add(entry)
    }

    override fun doRemoveAfter(index: Int) {
        if (index < doGetFirstLogIndex()) {
            entries.clear()
            nextLogIndex = logIndexOffset
        } else {
            entries.subList(index - logIndexOffset + 1, entries.size).clear()
            nextLogIndex = index + 1
        }
    }

    override fun commit(index: Int) {
        commitIndex = index
    }

    override var commitIndex: Int = 0

    override fun close() {}

    override fun toString(): String {
        return "MemoryEntrySequence(entries=$entries, commitIndex=$commitIndex)"
    }

}