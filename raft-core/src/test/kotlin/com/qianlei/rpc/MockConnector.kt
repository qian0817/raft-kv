package com.qianlei.rpc

import com.qianlei.node.NodeEndpoint
import com.qianlei.node.NodeId
import com.qianlei.rpc.message.AppendEntriesResult
import com.qianlei.rpc.message.AppendEntriesRpc
import com.qianlei.rpc.message.RequestVoteResult
import com.qianlei.rpc.message.RequestVoteRpc
import java.util.*
import kotlin.collections.ArrayList

/**
 * 测试用的 RPC 组件
 * @author qianlei
 */
class MockConnector : Connector {
    private val messages = LinkedList<Message>()

    override fun initialize() {}

    override fun sendRequestVote(rpc: RequestVoteRpc, destinationEndpoints: Collection<NodeEndpoint>) {
        val message = Message(rpc = rpc)
        messages.add(message)
    }

    override fun replyRequestVote(result: RequestVoteResult, destinationEndpoint: NodeEndpoint) {
        val message = Message(destinationNodeId = destinationEndpoint.id, result = result)
        messages.add(message)
    }

    override fun sendAppendEntries(rpc: AppendEntriesRpc, destinationEndpoint: NodeEndpoint) {
        val message = Message(rpc = rpc, destinationNodeId = destinationEndpoint.id)
        messages.add(message)
    }

    override fun replyAppendEntries(result: AppendEntriesResult, destinationEndpoint: NodeEndpoint) {
        val message = Message(result = result, destinationNodeId = destinationEndpoint.id)
        messages.add(message)
    }

    override fun close() {}

    fun getLastMessage(): Message? {
        return if (messages.isEmpty()) null else messages.last
    }

    private fun getLastMessageOrDefault(): Message {
        return if (messages.isEmpty()) Message() else messages.last
    }

    fun getRpc() = getLastMessageOrDefault().rpc

    fun getResult() = getLastMessageOrDefault().result

    fun getDestinationNodeId() = getLastMessageOrDefault().destinationNodeId

    fun getMessagesCount() = messages.size

    fun getMessages() = ArrayList(messages)

    fun clearMessage() = messages.clear()

    class Message(val rpc: Any? = null, val destinationNodeId: NodeId? = null, val result: Any? = null) {
        override fun toString(): String {
            return "Message(rpc=$rpc, destinationNodeId=$destinationNodeId, result=$result)"
        }
    }

}

