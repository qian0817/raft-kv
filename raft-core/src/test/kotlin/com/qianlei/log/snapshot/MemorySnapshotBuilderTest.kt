package com.qianlei.log.snapshot

import com.qianlei.node.NodeId
import com.qianlei.rpc.message.InstallSnapshotRpc
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class MemorySnapshotBuilderTest {
    @Test
    fun testSimple() {
        val data = "test".toByteArray()
        val rpc = InstallSnapshotRpc(
            2, NodeId.of("A"), 3, 2, 0, data, true
        )
        val builder = MemorySnapshotBuilder(rpc)
        val snapshot = builder.build()
        assertEquals(3, snapshot.lastIncludeIndex)
        assertEquals(2, snapshot.lastIncludeTerm)
        assertArrayEquals(data, snapshot.data)
    }

    @Test
    fun testAppend() {
        val firstRpc = InstallSnapshotRpc(
            2, NodeId.of("A"), 3, 2, 0, "test".toByteArray(), false
        )
        val builder = MemorySnapshotBuilder(firstRpc)
        val secondRpc = InstallSnapshotRpc(
            2, NodeId.of("A"), 3, 2, 4, "foo".toByteArray(), true
        )
        builder.append(secondRpc)
        val snapshot = builder.build()
        assertEquals(3, snapshot.lastIncludeIndex)
        assertEquals(2, snapshot.lastIncludeTerm)
        assertArrayEquals("testfoo".toByteArray(), snapshot.data)
    }

    @Test
    fun testAppendIllegalOffset() {
        val firstRpc = InstallSnapshotRpc(
            2, NodeId.of("A"), 3, 2, 0, "test".toByteArray(), false
        )
        val builder = MemorySnapshotBuilder(firstRpc)
        val secondRpc = InstallSnapshotRpc(
            2, NodeId.of("A"), 2, 2, 4, "foo".toByteArray(), true
        )
        assertThrows<IllegalArgumentException> { builder.append(secondRpc) }
    }

    @Test
    fun testAppendIllegalLastIncludedIndexOrTerm() {
        val firstRpc = InstallSnapshotRpc(
            2, NodeId.of("A"), 3, 2, 0, "test".toByteArray(), false
        )
        val builder = MemorySnapshotBuilder(firstRpc)
        val secondRpc = InstallSnapshotRpc(
            2, NodeId.of("A"), 2, 2, 4, "foo".toByteArray(), true
        )
        assertThrows<IllegalArgumentException> { builder.append(secondRpc) }
    }
}