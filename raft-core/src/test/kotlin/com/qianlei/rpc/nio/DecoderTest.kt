package com.qianlei.rpc.nio

import com.qianlei.node.NodeId
import com.qianlei.rpc.message.MessageConstants
import com.qianlei.rpc.message.RequestVoteRpc
import io.netty.buffer.Unpooled
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("EXPERIMENTAL_API_USAGE")
class DecoderTest {
    @Test
    fun testNodeId() {
        val byteBuf = Unpooled.buffer()
        byteBuf.writeInt(MessageConstants.MSG_TYPE_NODE_ID)
        byteBuf.writeInt(1)
        byteBuf.writeByte('A'.toInt())
        val decoder = Decoder()
        val out = arrayListOf<Any>()
        decoder.decode(null, byteBuf, out)
        assertEquals(NodeId.of("A"), out[0])
    }

    @Test
    fun testRequestVoteRpc() {
        val rpc = RequestVoteRpc(2, NodeId.of("A"), 1, 2)
        val byteBuf = Unpooled.buffer()
        byteBuf.writeInt(MessageConstants.MSG_TYPE_REQUEST_VOTE_RPC)
        val bytes = ProtoBuf.encodeToByteArray(rpc)
        byteBuf.writeInt(bytes.size)
        byteBuf.writeBytes(bytes)
        val decoder = Decoder()
        val out = arrayListOf<Any>()
        decoder.decode(null, byteBuf, out)
        assertEquals(out[0], rpc)
    }
}