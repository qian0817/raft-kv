package com.qianlei.log.sequence

import com.qianlei.log.entry.Entry
import com.qianlei.log.entry.NoOpEntry
import com.qianlei.support.ByteArraySeekableFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FileEntrySequenceTest {
    @Test
    fun testInitialize() {
        val entriesFile = EntriesFile(ByteArraySeekableFile())
        val entryIndexFile = EntryIndexFile(ByteArraySeekableFile())
        entryIndexFile.appendEntryIndex(1, 0, 1, 1)
        entryIndexFile.appendEntryIndex(2, 20, 1, 1)
        val sequence = FileEntrySequence(entriesFile, entryIndexFile, 1)
        assertEquals(3, sequence.nextLogIndex)
        assertEquals(1, sequence.firstLogIndex)
        assertEquals(2, sequence.lastLogIndex)
        assertEquals(2, sequence.commitIndex)
    }

    @Test
    fun testAppendEntry() {
        val entriesFile = EntriesFile(ByteArraySeekableFile())
        val entryIndexFile = EntryIndexFile(ByteArraySeekableFile())
        val sequence = FileEntrySequence(entriesFile, entryIndexFile, 1)
        assertEquals(1, sequence.nextLogIndex)
        sequence.append(NoOpEntry(1, 1))
        assertEquals(2, sequence.nextLogIndex)
        assertEquals(1, sequence.lastEntry?.index)
    }

    private fun appendEntryToFile(entry: Entry, entriesFile: EntriesFile, entryIndexFile: EntryIndexFile) {
        val offset = entriesFile.appendEntry(entry)
        entryIndexFile.appendEntryIndex(entry.index, offset, entry.kind, entry.term)
    }

    @Test
    fun testGetEntry() {
        val entriesFile = EntriesFile(ByteArraySeekableFile())
        val entryIndexFile = EntryIndexFile(ByteArraySeekableFile())
        appendEntryToFile(NoOpEntry(1, 1), entriesFile, entryIndexFile)
        val sequence = FileEntrySequence(entriesFile, entryIndexFile, 1)
        sequence.append(NoOpEntry(2, 1))
        assertNull(sequence.getEntry(0))
        assertEquals(1, sequence.getEntry(1)?.index)
        assertEquals(2, sequence.getEntry(2)?.index)
        assertNull(sequence.getEntry(3))
    }

    @Test
    fun testSubList() {
        val entriesFile = EntriesFile(ByteArraySeekableFile())
        val entryIndexFile = EntryIndexFile(ByteArraySeekableFile())
        appendEntryToFile(NoOpEntry(1, 1), entriesFile, entryIndexFile)
        appendEntryToFile(NoOpEntry(2, 2), entriesFile, entryIndexFile)
        val sequence = FileEntrySequence(entriesFile, entryIndexFile, 1)
        sequence.append(NoOpEntry(sequence.nextLogIndex, 3))
        sequence.append(NoOpEntry(sequence.nextLogIndex, 4))

        val subList = sequence.subList(2)
        assertEquals(3, subList.size)
        assertEquals(2, subList[0].index)
        assertEquals(4, subList[2].index)
    }

    @Test
    fun testRemoveAfterEntriesInFile() {
        val entriesFile = EntriesFile(ByteArraySeekableFile())
        val entryIndexFile = EntryIndexFile(ByteArraySeekableFile())
        appendEntryToFile(NoOpEntry(1, 1), entriesFile, entryIndexFile)
        appendEntryToFile(NoOpEntry(2, 1), entriesFile, entryIndexFile)
        val sequence = FileEntrySequence(entriesFile, entryIndexFile, 1)
        sequence.append(NoOpEntry(3, 2))
        assertEquals(1, sequence.firstLogIndex)
        assertEquals(3, sequence.lastLogIndex)
        sequence.removeAfter(1)
        assertEquals(1, sequence.firstLogIndex)
        assertEquals(1, sequence.lastLogIndex)

    }

}