package com.qianlei.log.snapshot

import com.qianlei.support.ByteArraySeekableFile
import org.junit.jupiter.api.Assertions.assertArrayEquals
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileSnapshotTest {
    @Test
    fun test() {
        val output = ByteArrayOutputStream()
        val writer = FileSnapshotWriter(output, 1, 2)
        val data = "test".toByteArray()
        writer.write(data)
        writer.close()
        val snapshot = FileSnapshot(ByteArraySeekableFile(output.toByteArray()))
        assertEquals(1, snapshot.lastIncludeIndex)
        assertEquals(2, snapshot.lastIncludeTerm)
        assertEquals(4, snapshot.dataSize)
        val chunk = snapshot.readData(0, 10)
        assertArrayEquals(data, chunk.data)
        assertTrue(chunk.lastChunk)
    }
}