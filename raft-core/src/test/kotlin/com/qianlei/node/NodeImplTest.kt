package com.qianlei.node

import com.qianlei.log.entry.Entry.Companion.KIND_NO_OP
import com.qianlei.node.role.CandidateNodeRole
import com.qianlei.node.role.FollowerNodeRole
import com.qianlei.node.role.LeaderNodeRole
import com.qianlei.node.store.MemoryNodeStore
import com.qianlei.rpc.MockConnector
import com.qianlei.rpc.message.*
import com.qianlei.schedule.NullSchedule
import com.qianlei.support.DirectTaskExecutor
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

/**
 *
 * @author qianlei
 */
class NodeImplTest {

    private fun newNodeBuilder(selfId: NodeId, vararg endpoints: NodeEndpoint): NodeBuilder {
        return NodeBuilder(endpoints.asList(), selfId)
            .setScheduler(NullSchedule())
            .setConnector(MockConnector())
            .setTaskExecutor(DirectTaskExecutor())
    }

    @Test
    fun testStart() {
        val node = newNodeBuilder(NodeId.of("A"), NodeEndpoint("A", "localhost", 2333)).build()
        node.start()
        assertTrue(node.role is FollowerNodeRole)
        val role = node.role as FollowerNodeRole
        assertEquals(role.term, 0)
        assertNull(role.votedFor)
    }

    @Test
    fun testStartLoadFromStore() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333)
        ).setStore(MemoryNodeStore(1, NodeId.of("B"))).build()
        node.start()
        val role = node.role
        assertTrue(role is FollowerNodeRole)
        assertEquals(role.term, 1)
        assertEquals(role.votedFor, NodeId.of("B"))
    }

    @Test
    fun testStop() {
        val node = newNodeBuilder(NodeId.of("A"), NodeEndpoint("A", "localhost", 2333)).build()
        node.start()
        node.stop()
    }

    @Test
    fun testStopIllegal() {
        val node = newNodeBuilder(NodeId.of("A"), NodeEndpoint("A", "localhost", 2333)).build()
        assertThrows<IllegalStateException> { node.stop() }
    }


    @Test
    fun testElectionTimeoutWhenLeader() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333)
        ).build()
        node.start()
        node.electionTimeout()
        node.electionTimeout() // do nothing
    }

    @Test
    fun testElectionTimeoutWhenFollower() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).build()
        node.start()
        node.electionTimeout()
        assertTrue(node.role is CandidateNodeRole)
        val role = node.role as CandidateNodeRole
        assertEquals(1, role.term)
        assertEquals(1, role.voteCount)
        val connector = node.context.connector as MockConnector
        val rpc = connector.getRpc()
        assertTrue(rpc is RequestVoteRpc)
        assertEquals(1, rpc.term)
        assertEquals(NodeId.of("A"), rpc.candidateId)
        assertEquals(0, rpc.lastLogIndex)
        assertEquals(0, rpc.lastLogTerm)
    }

    @Test
    fun testElectionTimeoutWhenCandidate() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).build()
        node.start()
        node.electionTimeout()
        node.electionTimeout()
        val role = node.role
        assertTrue(role is CandidateNodeRole)
        assertEquals(2, role.term)
        assertEquals(1, role.voteCount)
        val mockConnector = node.context.connector as MockConnector
        val rpc = mockConnector.getLastMessage()?.rpc as RequestVoteRpc
        assertEquals(2, rpc.term)
        assertEquals(NodeId.of("A"), rpc.candidateId)
        assertEquals(0, rpc.lastLogIndex)
        assertEquals(0, rpc.lastLogTerm)
    }

    @Test
    fun testOnReceiveRequestVoteRpcSmallerTerm() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        )
            .setStore(MemoryNodeStore(2, null))
            .build()
        node.start()
        val rpc = RequestVoteRpc(1, NodeId.of("C"))
        node.onReceiveRequestVoteRpc(RequestVoteRpcMessage(rpc, NodeId.of("C"), null))
        val mockConnector = node.context.connector as MockConnector
        val result = mockConnector.getResult() as RequestVoteResult
        assertEquals(2, result.term)
        assertFalse(result.voteGranted)
    }

    @Test
    fun testOnReceiveRequestVoteRpcLargerTerm() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        )
            .setStore(MemoryNodeStore(1, null))
            .build()
        node.start()
        val rpc = RequestVoteRpc(2, NodeId.of("C"), 1, 2)
        node.onReceiveRequestVoteRpc(RequestVoteRpcMessage(rpc, NodeId.of("C"), null))
        val mockConnector = node.context.connector as MockConnector
        val result = mockConnector.getResult() as RequestVoteResult
        assertEquals(2, result.term)
        assertTrue(result.voteGranted)
        val role = node.role
        assertTrue(role is FollowerNodeRole)
        assertEquals(NodeId.of("C"), role.votedFor)
        assertEquals(NodeId.of("C"), node.context.store.votedFor)
    }

    @Test
    fun testOnReceiveRequestVoteRpcLargerTermButNotVote() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(1, null)).build()
        node.context.log.appendEntry(1)
        node.start()
        val rpc = RequestVoteRpc(2, NodeId.of("C"))
        node.onReceiveRequestVoteRpc(RequestVoteRpcMessage(rpc, NodeId.of("C"), null))
        val mockConnector = node.context.connector as MockConnector
        val result = mockConnector.getResult() as RequestVoteResult
        assertEquals(2, result.term)
        assertFalse(result.voteGranted)
    }

    @Test
    fun testOnReceiveRequestVoteRpcFollower() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).build()
        node.start()
        val rpc = RequestVoteRpc(1, NodeId.of("C"), 0, 0)
        node.onReceiveRequestVoteRpc(RequestVoteRpcMessage(rpc, NodeId.of("C"), null))
        val connector = node.context.connector as MockConnector
        val result = connector.getResult() as RequestVoteResult
        assertEquals(1, result.term)
        assertTrue(result.voteGranted)
        assertEquals(NodeId.of("C"), (node.role as FollowerNodeRole).votedFor)
    }

    @Test
    fun testOnReceiveRequestVoteRpcFollowerVoted() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(1, NodeId.of("C"))).build()
        node.start()
        val rpc = RequestVoteRpc(1, NodeId.of("C"))
        node.onReceiveRequestVoteRpc(RequestVoteRpcMessage(rpc, NodeId.of("C"), null))
        val mockConnector = node.context.connector as MockConnector
        val result = mockConnector.getResult() as RequestVoteResult
        assertEquals(1, result.term)
        assertTrue(result.voteGranted)
        val role = node.role
        assertTrue(role is FollowerNodeRole)
        assertEquals(NodeId.of("C"), role.votedFor)
    }

    @Test
    fun testOnReceiveRequestVoteRpcFollowerNotVote() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(1, null)).build()
        node.context.log.appendEntry(1)
        node.start()
        val rpc = RequestVoteRpc(1, NodeId.of("C"))
        node.onReceiveRequestVoteRpc(RequestVoteRpcMessage(rpc, NodeId.of("C"), null))
        val mockConnector = node.context.connector as MockConnector
        val result = mockConnector.getResult() as RequestVoteResult
        assertEquals(1, result.term)
        assertFalse(result.voteGranted)
    }

    @Test
    fun testOnReceiveRequestVoteRpcCandidate() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(1, null)).build()
        node.start()
        node.electionTimeout()
        val rpc = RequestVoteRpc(2, NodeId.of("C"))
        node.onReceiveRequestVoteRpc(RequestVoteRpcMessage(rpc, NodeId.of("C"), null))
        val mockConnector = node.context.connector as MockConnector
        val result = mockConnector.getResult() as RequestVoteResult
        assertEquals(2, result.term)
        assertFalse(result.voteGranted)
    }

    @Test
    fun testOnReceiveRequestVoteRpcLeader() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(1, null)).build()
        node.start()
        node.electionTimeout()
        node.onReceiveRequestVoteResult(RequestVoteResult(2, true))
        val rpc = RequestVoteRpc(2, NodeId.of("C"))
        node.onReceiveRequestVoteRpc(RequestVoteRpcMessage(rpc, NodeId.of("C"), null))
        val mockConnector = node.context.connector as MockConnector
        val result = mockConnector.getResult() as RequestVoteResult
        assertEquals(2, result.term)
        assertFalse(result.voteGranted)
    }

    @Test
    fun testOnReceiveRequestVoteResult() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).build()
        node.start()
        node.electionTimeout()
        node.onReceiveRequestVoteResult(RequestVoteResult(1, true))
        assertTrue(node.role is LeaderNodeRole)
        assertEquals(1, node.role.term)

        assertTrue(node.context.group.findMember(NodeId.of("B")).isReplicationStateSet())
        assertTrue(node.context.group.findMember(NodeId.of("C")).isReplicationStateSet())

        val lastEntryMeta = node.context.log.lastEntryMeta
        assertEquals(KIND_NO_OP, lastEntryMeta.kind)
        assertEquals(1, lastEntryMeta.index)
        assertEquals(1, lastEntryMeta.term)
    }

    @Test
    fun testReplicationLog() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).build()
        node.start()
        node.electionTimeout()
        node.onReceiveRequestVoteResult(RequestVoteResult(1, true))
        node.replicateLog()
        val connector = node.context.connector as MockConnector
        assertEquals(3, connector.getMessagesCount())
        val messages = connector.getMessages()
        val destinationNodeIds = messages.subList(1, 3).map { it.destinationNodeId }.toList()
        assertEquals(2, destinationNodeIds.size)
        assertTrue(destinationNodeIds.contains(NodeId.of("B")))
        assertTrue(destinationNodeIds.contains(NodeId.of("C")))
        assertEquals(1, (messages[2].rpc as AppendEntriesRpc).term)
    }

    @Test
    fun testReplicateLogSkipReplicating() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).build()
        node.start()
        node.electionTimeout()
        node.onReceiveRequestVoteResult(RequestVoteResult(1, true))
        node.context.group.findMember(NodeId.of("B")).replicateNow()
        node.replicateLog()
        assertEquals(2, (node.context.connector as MockConnector).getMessagesCount())
    }

    @Test
    fun testOnReceiveAppendEntriesRpcFollower() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).build()
        node.start()
        val rpc = AppendEntriesRpc(0, NodeId.of("B"))
        node.onReceiveAppendEntriesRpc(AppendEntriesRpcMessage(rpc, NodeId.of("B")))
        val connector = node.context.connector as MockConnector
        val result = connector.getResult() as AppendEntriesResult
        assertEquals(0, result.term)
        assertTrue(result.success)
        val role = node.role as FollowerNodeRole
        assertEquals(0, role.term)
        assertEquals(NodeId.of("B"), role.leaderId)
    }

    @Test
    fun testOnReceiveAppendEntriesNormal() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).build()
        node.start()
        node.electionTimeout()
        node.onReceiveRequestVoteResult(RequestVoteResult(1, true))
        node.replicateLog()
        node.onReceiveAppendEntriesResult(
            AppendEntriesResultMessage(
                AppendEntriesResult(1, true),
                NodeId.of("B"),
                AppendEntriesRpc(1, NodeId.of("A"))
            )
        )
    }

    @Test
    fun testAppendLog() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).build()
        node.start()
        node.electionTimeout() // become candidate
        node.onReceiveRequestVoteResult(RequestVoteResult(1, true)) // become leader
        node.appendLog("test".toByteArray())
        val mockConnector = node.context.connector as MockConnector
        // request vote rpc + append entries * 2
        assertEquals(3, mockConnector.getMessagesCount())
    }

    @Test
    fun testOnReceiveInstallSnapshotRpcSmallerTerm() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(3, null)).build()
        node.start()
        val rpc = InstallSnapshotRpc(
            2,
            NodeId.of("B"),
            2,
            1,
            0,
            ByteArray(0),
            true
        )
        node.onReceiveInstallSnapshotRpc(InstallSnapshotRpcMessage(rpc, NodeId.of("B"), null))
        val mockConnector = node.context.connector as MockConnector
        val result = mockConnector.getResult() as InstallSnapshotResult
        assertEquals(3, result.term)
    }

    @Test
    fun testOnReceiveInstallSnapshotRpc() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(1, null)).build()
        node.start()
        val rpc = InstallSnapshotRpc(
            1,
            NodeId.of("B"),
            2,
            1,
            0,
            ByteArray(0),
            true
        )
        node.onReceiveInstallSnapshotRpc(InstallSnapshotRpcMessage(rpc, NodeId.of("B"), null))
        val mockConnector = node.context.connector as MockConnector
        val result = mockConnector.getResult() as InstallSnapshotResult
        assertEquals(1, result.term)
    }

    @Test
    fun testOnReceiveInstallSnapshotRpcLargerTerm() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(1, null)).build()
        node.start()
        val rpc = InstallSnapshotRpc(
            2,
            NodeId.of("B"),
            2,
            1,
            0,
            ByteArray(0),
            true
        )
        node.onReceiveInstallSnapshotRpc(InstallSnapshotRpcMessage(rpc, NodeId.of("B"), null))
        val mockConnector = node.context.connector as MockConnector
        val result = mockConnector.getResult() as InstallSnapshotResult
        assertEquals(2, result.term)
        val role = node.role
        assertTrue(role is FollowerNodeRole)
        assertEquals(NodeId.of("B"), role.leaderId)
    }

    @Test
    fun testOnReceiveInstallSnapshotResultLargerTerm() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(1, null)).build()
        node.start()
        val rpc = InstallSnapshotRpc(1, NodeId.of("C"), 1, 1, 0, ByteArray(0), true)
        node.onReceiveInstallSnapshotResult(
            InstallSnapshotResultMessage(InstallSnapshotResult(2), NodeId.of("C"), rpc)
        )
        val role = node.role
        assertTrue(role is FollowerNodeRole)
        assertEquals(2, role.term)
    }

    @Test
    fun testOnReceiveInstallSnapshotResultDone() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(1, null)).build()
        val mockConnector = node.context.connector as MockConnector
        node.start()
        node.electionTimeout()
        mockConnector.clearMessage()
        node.onReceiveRequestVoteResult(RequestVoteResult(2, true))
        val rpc = InstallSnapshotRpc(
            2,
            NodeId.of("C"),
            0,
            0,
            0,
            ByteArray(0),
            true
        )
        val snapshotResultMessage = InstallSnapshotResultMessage(InstallSnapshotResult(2), NodeId.of("C"), rpc)
        node.onReceiveInstallSnapshotResult(snapshotResultMessage)
        assertEquals(NodeId.of("C"), mockConnector.getDestinationNodeId())
        assertTrue(mockConnector.getRpc() is AppendEntriesRpc)
    }

    @Test
    fun testOnReceiveInstallSnapshotResultWhenNotLeader() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(1, null)).build()
        node.start()
        node.electionTimeout()
        val rpc = InstallSnapshotRpc(
            2,
            NodeId.of("B"),
            2,
            1,
            0,
            ByteArray(0),
            false
        )
        node.onReceiveInstallSnapshotResult(
            InstallSnapshotResultMessage(
                InstallSnapshotResult(2), NodeId.of("C"), rpc
            )
        )
    }

    @Test
    fun testOnReceiveInstallSnapshotResult() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        ).setStore(MemoryNodeStore(1, null)).build()
        val mockConnector = node.context.connector as MockConnector
        node.start()
        node.electionTimeout()
        mockConnector.clearMessage()
        node.onReceiveRequestVoteResult(RequestVoteResult(2, true))
        val rpc = InstallSnapshotRpc(
            2, NodeId.of("B"),
            2, 1, 0,
            ByteArray(0), false
        )
        node.onReceiveInstallSnapshotResult(
            InstallSnapshotResultMessage(
                InstallSnapshotResult(2), NodeId.of("C"), rpc
            )
        )
        assertEquals(NodeId.of("C"), mockConnector.getDestinationNodeId())
        assertTrue(mockConnector.getRpc() is InstallSnapshotRpc)
    }

}