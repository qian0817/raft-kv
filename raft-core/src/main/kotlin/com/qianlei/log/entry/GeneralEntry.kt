package com.qianlei.log.entry

import com.qianlei.log.entry.Entry.Companion.KIND_GENERAL

/**
 * 普通日志条目
 * @author qianlei
 */
class GeneralEntry(
    override val index: Int,
    override val term: Int,
    override val commandBytes: ByteArray
) : AbstractEntry(KIND_GENERAL, index, term) {

    override fun toString(): String {
        return "GeneralEntry(index=$index, term=$term)"
    }
}