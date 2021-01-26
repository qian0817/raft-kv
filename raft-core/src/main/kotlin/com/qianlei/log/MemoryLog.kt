package com.qianlei.log

import com.qianlei.log.sequence.EntrySequence
import com.qianlei.log.sequence.MemoryEntrySequence

/**
 *
 * @author qianlei
 */
class MemoryLog(entrySequence: EntrySequence = MemoryEntrySequence()) : AbstractLog() {
    init {
        this.entrySequence = entrySequence
    }

    override fun close() {
        entrySequence.close()
    }
}