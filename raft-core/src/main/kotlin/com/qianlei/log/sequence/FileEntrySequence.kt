package com.qianlei.log.sequence

import com.qianlei.log.LogDir
import com.qianlei.log.entry.Entry
import java.util.*
import kotlin.math.min

/**
 * 基于文件的日志序列实现，主要涉及三个部分
 * 1. 日志条目文件 EntriesFile
 * 是一个包含全部日志条目内容的二进制文件，该文件按照顺序从第一条存放到最后一条
 * 其格式为
 *  四字节   四字节   四字节   四字节
 * -----------------------------------------------
 * | kind | index | term | length | commandBytes |
 * | kind | index | term | length | commandBytes |
 * -----------------------------------------------
 * 2. 日志条目索引文件 entryIndexFile
 * 是一个包含日志条目元信息的二进制文件，包含起始索引、结束索引、位置偏移、日志类型以及日志 term
 * 其格式为
 *         四字节          四字节
 * ----------------------------------------------------
 * | minEntryIndex | maxEntryIndex |
 * |            offset             |   kind   |    term  |
 * |            offset             |   kind   |    term  |
 * ----------------------------------------------------
 *            八字节                 四字节        四字节
 * 3. 等待写入的日志条目缓冲 pendingEntries
 * 包含未写入文件的日志条目，提交日志时移除前面的日志
 *
 * @author qianlei
 */
class FileEntrySequence(
    private val entriesFile: EntriesFile,
    private val entryIndexFile: EntryIndexFile,
    logIndexOffset: Int
) : AbstractEntrySequence(logIndexOffset) {
    private val pendingEntries = LinkedList<Entry>()
    override var commitIndex: Int = 0

    init {
        initialize()
    }

    constructor(logDir: LogDir, logIndexOffset: Int) : this(
        EntriesFile(logDir.getEntriesFile()),
        EntryIndexFile(logDir.getEntryOffsetIndexFile()),
        logIndexOffset
    )

    private fun initialize() {
        if (entryIndexFile.isEmpty()) {
            commitIndex = logIndexOffset - 1
            return
        }
        logIndexOffset = entryIndexFile.minEntryIndex
        nextLogIndex = entryIndexFile.maxEntryIndex + 1
        commitIndex = entryIndexFile.maxEntryIndex
    }

    override fun doGetEntry(index: Int): Entry {
        if (!pendingEntries.isEmpty()) {
            val firstPendingEntryIndex = pendingEntries.first.index
            if (index >= firstPendingEntryIndex) {
                return pendingEntries[index - firstPendingEntryIndex]
            }
        }
        return getEntryInFile(index)
    }

    override fun doSubList(fromIndex: Int, toIndex: Int): List<Entry> {
        val result = arrayListOf<Entry>()
        // 从文件中读取日志条目
        if (!entryIndexFile.isEmpty() && fromIndex <= entryIndexFile.maxEntryIndex) {
            val maxIndex = min(entryIndexFile.maxEntryIndex + 1, toIndex)
            for (i in fromIndex until maxIndex) {
                result.add(getEntryInFile(i))
            }
        }

        // 从日志缓冲中获取日志目录
        if (!pendingEntries.isEmpty() && toIndex > pendingEntries.first.index) {
            val iterator = pendingEntries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val index = entry.index
                if (index >= toIndex) {
                    break
                }
                if (index >= fromIndex) {
                    result.add(entry)
                }
            }
        }

        return result
    }

    private fun getEntryInFile(index: Int): Entry {
        val offset = entryIndexFile.getOffset(index)
        return entriesFile.loadEntry(offset)
    }

    override fun doAppend(entry: Entry) {
        pendingEntries.add(entry)
    }

    override fun doRemoveAfter(index: Int) {
        // 只需要移除缓冲中的日志条目
        if (!pendingEntries.isEmpty() && index >= pendingEntries.first.index - 1) {
            for (i in index + 1..doGetLastLogIndex()) {
                pendingEntries.removeLast()
            }
            nextLogIndex = index + 1
            return
        }
        if (index >= doGetFirstLogIndex()) {
            pendingEntries.clear()
            entriesFile.truncate(entryIndexFile.getOffset(index + 1))
            entryIndexFile.removeAfter(index)
            nextLogIndex = index + 1
            commitIndex = index
        } else {
            // 如果索引比日志缓冲中的第一条日志小，那么清除所有数据
            pendingEntries.clear()
            entriesFile.clear()
            entryIndexFile.clear()
            nextLogIndex = logIndexOffset
            commitIndex = logIndexOffset - 1
        }
    }

    override fun commit(index: Int) {
        require(index >= commitIndex) { "commit index $index < commitIndex $commitIndex" }
        if (index == commitIndex) {
            return
        }
        require(!pendingEntries.isEmpty() && pendingEntries.last.index >= index) {
            "no entry to commit or commit index exceed"
        }

        for (i in pendingEntries.first.index..index) {
            val entry = pendingEntries.removeFirst()
            val offset = entriesFile.appendEntry(entry)
            entryIndexFile.appendEntryIndex(i, offset, entry.kind, entry.term)
            commitIndex = i
        }
    }

    override fun close() {
        entriesFile.close()
        entryIndexFile.close()
    }
}