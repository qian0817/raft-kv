package com.qianlei.log.sequence

import com.qianlei.log.entry.EntryMeta

/**
 *
 * @author qianlei
 */
data class EntryIndexItem(
    val index: Int = 0,
    val offset: Long = 0L,
    val kind: Int = 0,
    val term: Int = 0
) {
    fun toEntryMeta(): EntryMeta = EntryMeta(kind, index, term)
}