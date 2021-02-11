package com.qianlei.node

import com.qianlei.node.role.CandidateNodeRole
import com.qianlei.node.role.FollowerNodeRole
import com.qianlei.node.role.LeaderNodeRole
import com.qianlei.rpc.MockConnector
import com.qianlei.rpc.message.*
import com.qianlei.schedule.NullSchedule
import com.qianlei.support.DirectTaskExecutor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 *
 * @author qianlei
 */
class NodeImplTest {

    private fun newNodeBuilder(selfId: NodeId, vararg endpoints: NodeEndpoint): NodeImpl {
        return NodeBuilder(endpoints.asList(), selfId)
            .setScheduler(NullSchedule())
            .setConnector(MockConnector())
            .setTaskExecutor(DirectTaskExecutor())
            .build()
    }

    @Test
    fun testStart() {
        val node = newNodeBuilder(NodeId.of("A"), NodeEndpoint("A", "localhost", 2333))
        node.start()
        assertTrue(node.role is FollowerNodeRole)
        val role = node.role as FollowerNodeRole
        assertEquals(role.term, 0)
        assertNull(role.votedFor)
    }

    @Test
    fun testElectionTimeoutWhenFollower() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        )
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
    fun testOnReceiveRequestVoteRpcFollower() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        )
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
    fun testOnReceiveRequestVoteResult() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        )
        node.start()
        node.electionTimeout()
        node.onReceiveRequestVoteResult(RequestVoteResult(1, true))
        assertTrue(node.role is LeaderNodeRole)
        assertEquals(1, node.role.term)
    }

    @Test
    fun testReplicationLog() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        )
        node.start()
        node.electionTimeout()
        node.onReceiveRequestVoteResult(RequestVoteResult(1, true))
        node.replicateLog()
        val connector = node.context.connector as MockConnector
        assertEquals(3,connector.getMessagesCount())
        val messages = connector.getMessages()
        val destinationNodeIds = messages.subList(1, 3).map { it.destinationNodeId }.toList()
        assertEquals(2, destinationNodeIds.size)
        assertTrue(destinationNodeIds.contains(NodeId.of("B")))
        assertTrue(destinationNodeIds.contains(NodeId.of("C")))
        assertEquals(1, (messages[2].rpc as AppendEntriesRpc).term)
    }

    @Test
    fun testOnReceiveAppendEntriesRpcFollower() {
        val node = newNodeBuilder(
            NodeId.of("A"),
            NodeEndpoint("A", "localhost", 2333),
            NodeEndpoint("B", "localhost", 2334),
            NodeEndpoint("C", "localhost", 2335)
        )
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
        )
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

}