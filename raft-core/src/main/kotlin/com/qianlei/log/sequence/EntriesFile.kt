package com.qianlei.log.sequence

import com.qianlei.log.entry.Entry
import com.qianlei.log.entry.EntryFactory
import com.qianlei.support.RandomAccessFileAdapter
import com.qianlei.support.SeekableFile
import java.io.File

/**
 *
 *
 * @author qianlei
 */
class EntriesFile(private val seekableFile: SeekableFile) {
    constructor(file: File) : this(RandomAccessFileAdapter(file))

    /**
     * 追加日志条目
     */
    fun appendEntry(entry: Entry): Long {
        // 定位到文件末尾
        val offset = seekableFile.size()
        seekableFile.seek(offset)
        // 依次写入 kind index term length commandBytes
        seekableFile.writeInt(entry.kind)
        seekableFile.writeInt(entry.index)
        seekableFile.writeInt(entry.term)
        seekableFile.writeInt(entry.commandBytes.size)
        seekableFile.write(entry.commandBytes)
        return offset
    }

    /**
     * 从指定偏移加载日志条目
     */
    fun loadEntry(offset: Long, factory: EntryFactory): Entry {
        if (offset > seekableFile.size()) {
            throw IllegalArgumentException("offset > size")
        }
        seekableFile.seek(offset)
        val kind = seekableFile.readInt()
        val index = seekableFile.readInt()
        val term = seekableFile.readInt()
        val length = seekableFile.readInt()
        val bytes = ByteArray(length)
        seekableFile.read(bytes)
        return factory.create(kind, index, term, bytes)
    }

    /**
     * 日志大小
     */
    fun size(): Long = seekableFile.size()

    /**
     * 清除所有内容
     */
    fun clear() = truncate(0)

    /**
     * 清除指定区域后的内容
     */
    fun truncate(offset: Long) = seekableFile.truncate(offset)

    /**
     * 关闭文件
     */
    fun close() = seekableFile.close()
}