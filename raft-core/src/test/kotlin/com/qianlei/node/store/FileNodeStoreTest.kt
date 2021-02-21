package com.qianlei.node.store

import com.qianlei.node.NodeId
import com.qianlei.support.ByteArraySeekableFile
import org.junit.jupiter.api.Assertions.assertArrayEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FileNodeStoreTest {
    @Test
    fun testRead() {
        val file = ByteArraySeekableFile()
        file.writeInt(1)
        file.writeInt(1)
        file.write("A".toByteArray())
        file.seek(0L)
        val store = FileNodeStore(file)
        assertEquals(1, store.term)
        assertEquals(NodeId.of("A"), store.votedFor)
    }

    @Test
    fun testRead2() {
        val file = ByteArraySeekableFile()
        file.writeInt(1)
        file.writeInt(0)
        file.seek(0L)
        val store = FileNodeStore(file)
        assertEquals(1, store.term)
        assertNull(store.votedFor)
    }

    @Test
    fun testWrite() {
        val file = ByteArraySeekableFile()
        val store = FileNodeStore(file)
        assertEquals(0, store.term)
        assertNull(store.votedFor)
        assertEquals(8, file.size())
        store.term = 1
        val nodeId = NodeId("A")
        store.votedFor = nodeId

        // (term, 4) + (votedFor length, 4) + (votedFor data, 1) = 9
        assertEquals(9, file.size())
        file.seek(0)
        assertEquals(1, file.readInt())
        assertEquals(1, file.readInt())
        val data = ByteArray(1)
        file.read(data)
        assertArrayEquals(nodeId.value.toByteArray(), data)
        assertEquals(1, store.term)
        assertEquals(nodeId, store.votedFor)
    }
}