package com.qianlei.rpc.nio

import com.qianlei.node.NodeId
import com.qianlei.rpc.message.MessageConstants
import com.qianlei.rpc.message.RequestVoteRpc
import io.netty.buffer.Unpooled
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("EXPERIMENTAL_API_USAGE")
class EncoderTest {
    @Test
    fun testNodeId() {
        val encoder = Encoder()
        val buffer = Unpooled.buffer()
        encoder.encode(null, NodeId.of("A"), buffer)
        assertEquals(MessageConstants.MSG_TYPE_NODE_ID, buffer.readInt())
        assertEquals(1, buffer.readInt())
        assertEquals('A'.toByte(), buffer.readByte())
    }

    @Test
    fun testRequestVoteRpc() {
        val encoder = Encoder()
        val buffer = Unpooled.buffer()
        val rpc = RequestVoteRpc(2, NodeId.of("A"), 2, 1)
        encoder.encode(null, rpc, buffer)
        assertEquals(MessageConstants.MSG_TYPE_REQUEST_VOTE_RPC, buffer.readInt())
        val size = buffer.readInt()
        val byteArray = ByteArray(size)
        buffer.readBytes(byteArray)
        val decodeRpc = ProtoBuf.decodeFromByteArray<RequestVoteRpc>(byteArray)
        assertEquals(decodeRpc, rpc)
    }
}