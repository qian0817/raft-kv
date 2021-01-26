package com.qianlei.log.sequence

import com.qianlei.log.entry.Entry.Companion.KIND_GENERAL
import com.qianlei.support.ByteArraySeekableFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntryIndexFileTest {
    private fun makeEntryIndexFileContent(minEntryIndex: Int, maxEntryIndex: Int): ByteArraySeekableFile {
        val file = ByteArraySeekableFile()
        file.writeInt(minEntryIndex)
        file.writeInt(maxEntryIndex)
        for (i in minEntryIndex..maxEntryIndex) {
            file.writeLong(10L * i)
            file.writeInt(KIND_GENERAL)
            file.writeInt(i)
        }
        file.seek(0)
        return file
    }

    @Test
    fun testLoad() {
        val seekableFile = makeEntryIndexFileContent(3, 4)
        val file = EntryIndexFile(seekableFile)
        assertEquals(3, file.minEntryIndex)
        assertEquals(4, file.maxEntryIndex)
        assertEquals(2, file.entryIndexCount)
        var item = file.get(3)
        assertEquals(30L, item.offset)
        assertEquals(KIND_GENERAL, item.kind)
        assertEquals(3, item.term)
        item = file.get(4)
        assertEquals(40L, item.offset)
        assertEquals(KIND_GENERAL, item.kind)
        assertEquals(4, item.term)
    }

    @Test
    fun testAppendEntryIndex() {
        val seekableFile = ByteArraySeekableFile()
        val file = EntryIndexFile(seekableFile)
        file.appendEntryIndex(10, 100, 1, 2)
        assertEquals(1, file.entryIndexCount)
        assertEquals(10, file.minEntryIndex)
        assertEquals(10, file.maxEntryIndex)
        seekableFile.seek(0L)
        assertEquals(10, seekableFile.readInt())
        assertEquals(10, seekableFile.readInt())
        assertEquals(100L, seekableFile.readLong())
        assertEquals(1, seekableFile.readInt())
        assertEquals(2, seekableFile.readInt())
        val item = file.get(10)
        assertEquals(100L, item.offset)
        assertEquals(1, item.kind)
        assertEquals(2, item.term)
        file.appendEntryIndex(11, 200, 1, 2)
        assertEquals(2, file.entryIndexCount)
        assertEquals(10, file.minEntryIndex)
        assertEquals(11, file.maxEntryIndex)
        seekableFile.seek(24)
        assertEquals(200, seekableFile.readLong())
        assertEquals(1, seekableFile.readInt())
        assertEquals(2, seekableFile.readInt())
    }

    @Test
    fun testClear() {
        val seekableFile = makeEntryIndexFileContent(5, 6)
        val file = EntryIndexFile(seekableFile)
        assertFalse(file.isEmpty())
        file.clear()
        assertTrue(file.isEmpty())
        assertEquals(0, file.entryIndexCount)
        assertEquals(0, seekableFile.size())
    }

    @Test
    fun testRemoveAfter() {
        val seekableFile = makeEntryIndexFileContent(5, 6)
        val oldSize = seekableFile.size()
        val file = EntryIndexFile(seekableFile)
        file.removeAfter(6)
        assertEquals(5, file.minEntryIndex)
        assertEquals(6, file.maxEntryIndex)
        assertEquals(oldSize, seekableFile.size())
        assertEquals(2, file.entryIndexCount)
    }

    @Test
    fun testGet() {
        val file = EntryIndexFile(makeEntryIndexFileContent(3, 4))
        val item = file.get(3)
        assertEquals(KIND_GENERAL, item.kind)
        assertEquals(3, item.term)
    }

    @Test
    fun testIterator() {
        val file = EntryIndexFile(makeEntryIndexFileContent(3, 4))
        val iterator = file.iterator()
        assertTrue(iterator.hasNext())
        var item = iterator.next()
        assertEquals(item.index, 3)
        assertEquals(item.kind, 1)
        assertEquals(item.term, 3)
        assertTrue(iterator.hasNext())
        item = iterator.next()
        assertEquals(item.index, 4)
        assertFalse(iterator.hasNext())
    }
}