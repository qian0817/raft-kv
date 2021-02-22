package com.qianlei.log.entry

/**
 *
 * @author qianlei
 */
object EntryFactory {
    fun create(kind: Int, index: Int, term: Int, commandBytes: ByteArray): Entry {
        return when (kind) {
            Entry.KIND_NO_OP -> NoOpEntry(index, term)
            Entry.KIND_GENERAL -> GeneralEntry(index, term, commandBytes)
            else -> throw IllegalArgumentException("unexpected entry kind $kind")
        }
    }
}