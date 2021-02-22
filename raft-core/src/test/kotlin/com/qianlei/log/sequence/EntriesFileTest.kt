package com.qianlei.log.sequence

import com.qianlei.log.entry.Entry
import com.qianlei.log.entry.GeneralEntry
import com.qianlei.log.entry.NoOpEntry
import com.qianlei.support.ByteArraySeekableFile
import org.junit.jupiter.api.Assertions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntriesFileTest {

    @Test
    fun testAppendEntry() {
        val seekableFile = ByteArraySeekableFile()
        val file = EntriesFile(seekableFile)
        assertEquals(0L, file.appendEntry(NoOpEntry(2, 3)))
        seekableFile.seek(0)
        assertEquals(Entry.KIND_NO_OP, seekableFile.readInt())
        assertEquals(2, seekableFile.readInt())
        assertEquals(3, seekableFile.readInt())
        assertEquals(0, seekableFile.readInt())
        val commandBytes = "test".encodeToByteArray()
        assertEquals(16L, file.appendEntry(GeneralEntry(3, 3, commandBytes)))
        seekableFile.seek(16)
        assertEquals(Entry.KIND_GENERAL, seekableFile.readInt())
        assertEquals(3, seekableFile.readInt())
        assertEquals(3, seekableFile.readInt())
        assertEquals(4, seekableFile.readInt())
        val buffer = ByteArray(4)
        seekableFile.read(buffer)
        Assertions.assertArrayEquals(commandBytes, buffer)
    }

    @Test
    fun testLoadEntry() {
        val seekableFile = ByteArraySeekableFile()
        val file = EntriesFile(seekableFile)
        assertEquals(0L, file.appendEntry(NoOpEntry(2, 3)))
        assertEquals(16L, file.appendEntry(GeneralEntry(3, 3, "test".encodeToByteArray())))
        assertEquals(36L, file.appendEntry(GeneralEntry(4, 3, "foo".encodeToByteArray())))
        var entry = file.loadEntry(0L)
        assertEquals(Entry.KIND_NO_OP, entry.kind)
        assertEquals(2, entry.index)
        assertEquals(3, entry.term)
        entry = file.loadEntry(36L)
        assertEquals(Entry.KIND_GENERAL, entry.kind)
        assertEquals(4, entry.index)
        assertEquals(3, entry.term)
        Assertions.assertArrayEquals("foo".encodeToByteArray(), entry.commandBytes)
    }

    @Test
    fun testTruncate() {
        val seekableFile = ByteArraySeekableFile()
        val file = EntriesFile(seekableFile)
        file.appendEntry(NoOpEntry(2, 3))
        assertTrue(seekableFile.size() > 0)
        file.truncate(0)
        assertEquals(0, seekableFile.size())
    }
}