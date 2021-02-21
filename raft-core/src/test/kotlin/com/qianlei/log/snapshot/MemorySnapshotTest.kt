package com.qianlei.log.snapshot

import org.junit.jupiter.api.Assertions.assertArrayEquals
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemorySnapshotTest {
    @Test
    fun testReadEmpty() {
        val snapshot = MemorySnapshot(0, 0)
        snapshot.readData(0, 10)
    }

    @Test
    fun testRead1() {
        val snapshot = MemorySnapshot(0, 0, "foo".toByteArray())
        val chunk1 = snapshot.readData(0, 2)
        assertArrayEquals(byteArrayOf('f'.toByte(), 'o'.toByte()), chunk1.data)
        assertFalse(chunk1.lastChunk)
        val lastChunk = snapshot.readData(2, 2)
        assertArrayEquals(byteArrayOf('o'.toByte()), lastChunk.data)
        assertTrue(lastChunk.lastChunk)
    }

    @Test
    fun testRead2() {
        val snapshot = MemorySnapshot(0, 0, "foo,".toByteArray())
        assertArrayEquals(byteArrayOf('f'.toByte(), 'o'.toByte()), snapshot.readData(0, 2).data)
        assertArrayEquals(byteArrayOf('o'.toByte(), ','.toByte()), snapshot.readData(2, 2).data)
    }
}