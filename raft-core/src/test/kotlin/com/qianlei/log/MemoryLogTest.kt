package com.qianlei.log

import com.google.common.eventbus.EventBus
import com.qianlei.log.Log.Companion.ALL_ENTRIES
import com.qianlei.log.entry.NoOpEntry
import com.qianlei.node.NodeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("UnstableApiUsage")
class MemoryLogTest {

    @Test
    fun testCreateAppendEntriesRpcStartFromOne() {
        val log = MemoryLog(eventBus = EventBus())
        log.appendEntry(1)
        log.appendEntry(1)
        val rpc = log.createAppendEntriesRpc(1, NodeId.of("A"), 1, ALL_ENTRIES)
        assertEquals(1, rpc.term)
        assertEquals(0, rpc.prevLogIndex)
        assertEquals(0, rpc.prevLogTerm)
        assertEquals(2, rpc.entries.size)
        assertEquals(1, rpc.entries[0].index)
    }

    @Test
    fun testAppendEntriesFromLeaderSkip() {
        val log = MemoryLog(eventBus = EventBus())
        log.appendEntry(1)
        log.appendEntry(1)
        val leaderEntries = listOf(NoOpEntry(2, 1), NoOpEntry(3, 2))
        assertTrue(log.appendEntriesFromLeader(1, 1, leaderEntries))
    }

    @Test
    fun testAppendEntriesFromLeaderConflict() {
        val log = MemoryLog(eventBus = EventBus())
        log.appendEntry(1)
        log.appendEntry(1)
        val leaderEntries = listOf(NoOpEntry(2, 2), NoOpEntry(3, 2))
        assertTrue(log.appendEntriesFromLeader(1, 1, leaderEntries))
    }
}