package com.qianlei.log.sequence

import com.qianlei.log.entry.NoOpEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MemoryEntrySequenceTest {
    @Test
    fun testAppendEntry() {
        val sequence = MemoryEntrySequence()
        sequence.append(NoOpEntry(sequence.nextLogIndex, 1))
        assertEquals(2, sequence.nextLogIndex)
        assertEquals(1, sequence.lastLogIndex)
    }

    @Test
    fun testGetEntry() {
        val sequence = MemoryEntrySequence(2)
        sequence.append(listOf(NoOpEntry(2, 1), NoOpEntry(3, 1)))
        assertNull(sequence.getEntry(1))
        assertEquals(2, sequence.getEntry(2)?.index)
        assertEquals(3, sequence.getEntry(3)?.index)
        assertNull(sequence.getEntry(4))
    }

    @Test
    fun testGetEntryMeta() {
        val sequence = MemoryEntrySequence(2)
        assertNull(sequence.getEntry(2))
        sequence.append(NoOpEntry(2, 1))
        val meta = sequence.getEntryMeta(2)
        assertNotNull(meta)
        assertEquals(2, meta.index)
        assertEquals(1, meta.term)
    }

    @Test
    fun testSubListOneElement() {
        val sequence = MemoryEntrySequence(2)
        sequence.append(listOf(NoOpEntry(2, 1), NoOpEntry(3, 1)))
        val subList = sequence.subList(2, 3)
        assertEquals(1, subList.size)
        assertEquals(2, subList[0].index)
    }
}