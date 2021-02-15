package com.qianlei.log.snapshot

import java.io.Closeable
import java.io.InputStream

/**
 *
 * @author qianlei
 */
interface Snapshot : Closeable {
    /** 最后一条日志的索引 */
    val lastIncludeIndex: Int

    /** 最后一条日志的 term */
    val lastIncludeTerm: Int

    /** 数据长度 */
    val dataSize: Long

    /** 读取指定位置和长度的数据 */
    fun readData(offset: Int, length: Int): SnapshotChunk

    /** 读取数据流 */
    val dataStream: InputStream
}