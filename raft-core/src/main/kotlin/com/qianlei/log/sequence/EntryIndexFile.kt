package com.qianlei.log.sequence

import com.qianlei.support.RandomAccessFileAdapter
import com.qianlei.support.SeekableFile
import java.io.File
import java.util.*

/**
 *
 * @author qianlei
 */
class EntryIndexFile(
    private val seekableFile: SeekableFile
) : Iterable<EntryIndexItem> {


    private val entryIndexMap = hashMapOf<Int, EntryIndexItem>()

    init {
        load()
    }

    companion object {
        /**'
         * 最大条目索引的偏移
         */
        private const val OFFSET_MAX_ENTRY_INDEX = Integer.BYTES.toLong()

        /**
         * 单条日志条目元信息的长度
         */
        private const val LENGTH_ENTRY_INDEX_ITEM = 16
    }

    /**
     * 日志条目数
     */
    var entryIndexCount = 0
        private set

    /**
     * 最小日志索引
     */
    var minEntryIndex = 0
        private set

    /**
     * 最大日志索引
     */
    var maxEntryIndex = 0
        private set

    constructor(file: File) : this(RandomAccessFileAdapter(file))

    private fun load() {
        // 空文件判断
        if (seekableFile.size() == 0L) {
            entryIndexCount = 0
            return
        }
        minEntryIndex = seekableFile.readInt()
        maxEntryIndex = seekableFile.readInt()
        updateEntryIndexCount()
        for (i in minEntryIndex..maxEntryIndex) {
            val offset = seekableFile.readLong()
            val kind = seekableFile.readInt()
            val term = seekableFile.readInt()
            entryIndexMap[i] = EntryIndexItem(i, offset, kind, term)
        }
    }

    fun getOffset(entryIndex: Int): Long {
        return get(entryIndex).offset
    }

    private fun checkEmpty() = check(!isEmpty()) { "no entry index" }


    operator fun get(entryIndex: Int): EntryIndexItem {
        checkEmpty()
        require(!(entryIndex < minEntryIndex || entryIndex > maxEntryIndex)) {
            "index < min or index > max, " +
                    "enterIndex is $entryIndex ," +
                    "minEntryIndex is $minEntryIndex, " +
                    "maxEntryIndex is $maxEntryIndex "
        }
        return entryIndexMap.getValue(entryIndex)
    }

    /**
     * 更新日志条目数量
     */
    private fun updateEntryIndexCount() {
        entryIndexCount = maxEntryIndex - minEntryIndex + 1
    }

    fun appendEntryIndex(index: Int, offset: Long, kind: Int, term: Int) {
        if (seekableFile.size() == 0L) {
            // 如果为空文件，那么需要写入 minEntryIndex
            seekableFile.writeInt(index)
            minEntryIndex = index
        } else {
            // 检查索引
            if (index != maxEntryIndex + 1) {
                throw IllegalArgumentException("index must be ${maxEntryIndex + 1} but was $index")
            }
            seekableFile.seek(OFFSET_MAX_ENTRY_INDEX)
        }
        // 写入 maxEntryIndex
        seekableFile.writeInt(index)
        maxEntryIndex = index
        updateEntryIndexCount()
        // 移动到文件最后
        seekableFile.seek(getOffsetOfEntryIndexItem(index))
        seekableFile.writeLong(offset)
        seekableFile.writeInt(kind)
        seekableFile.writeInt(term)
        entryIndexMap[index] = EntryIndexItem(index, offset, kind, term)
    }

    /**
     * 获取指定索引日志的偏移
     */
    private fun getOffsetOfEntryIndexItem(index: Int): Long {
        return ((index - minEntryIndex) * LENGTH_ENTRY_INDEX_ITEM + Integer.BYTES * 2).toLong()
    }

    fun clear() {
        seekableFile.truncate(0)
        entryIndexCount = 0
        entryIndexMap.clear()
    }

    fun isEmpty(): Boolean {
        return entryIndexCount == 0
    }

    /**
     * 移除某个索引之后的数据
     */
    fun removeAfter(newMaxEntryIndex: Int) {
        // 判断是否为空
        if (isEmpty() || newMaxEntryIndex >= maxEntryIndex) {
            return
        }
        if (newMaxEntryIndex < minEntryIndex) {
            clear()
            return
        }
        seekableFile.seek(OFFSET_MAX_ENTRY_INDEX)
        seekableFile.writeInt(newMaxEntryIndex)
        seekableFile.truncate(getOffsetOfEntryIndexItem(newMaxEntryIndex + 1))
        // 清除缓存中的元信息
        for (i in newMaxEntryIndex..maxEntryIndex) {
            entryIndexMap.remove(i)
        }
        maxEntryIndex = newMaxEntryIndex
        updateEntryIndexCount()
    }

    /**
     * 遍历所有日志条目元信息
     */
    override fun iterator(): Iterator<EntryIndexItem> {
        if (isEmpty()) {
            return Collections.emptyIterator()
        }
        return EntryIndexIterator(entryIndexCount, minEntryIndex)
    }

    private inner class EntryIndexIterator(
        private val entryIndexCount: Int,
        private var currentEntryIndex: Int
    ) : Iterator<EntryIndexItem> {
        override fun hasNext(): Boolean {
            checkModification()
            return currentEntryIndex <= maxEntryIndex
        }

        private fun checkModification() {
            if (entryIndexCount != this@EntryIndexFile.entryIndexCount) {
                throw IllegalStateException("entry index count changed")
            }
        }

        override fun next(): EntryIndexItem {
            checkModification()
            return entryIndexMap.getValue(currentEntryIndex++)
        }
    }

    fun close() {
        seekableFile.close()
    }
}

