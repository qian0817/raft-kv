package com.qianlei.log.entry

import com.qianlei.log.entry.Entry.Companion.KIND_NO_OP

/**
 * 空日志
 *
 * @author qianlei
 */
class NoOpEntry(
    override val index: Int,
    override val term: Int
) : AbstractEntry(KIND_NO_OP, index, term) {
    override val commandBytes: ByteArray
        get() = byteArrayOf()

    override fun toString(): String {
        return "NoOpEntry(index=$index, term=$term)"
    }

}