package com.qianlei.rpc.nio

import com.qianlei.node.NodeId
import com.qianlei.rpc.message.MessageConstants
import com.qianlei.rpc.message.RequestVoteRpc
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 *
 * @author qianlei
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class Encoder : MessageToByteEncoder<Any>() {
    public override fun encode(ctx: ChannelHandlerContext?, msg: Any, out: ByteBuf) {
        if (msg is NodeId) {
            writeMessage(out, MessageConstants.MSG_TYPE_NODE_ID, msg.value.encodeToByteArray())
        } else if (msg is RequestVoteRpc) {
            val bytes = ProtoBuf.encodeToByteArray(msg)
            writeMessage(out, MessageConstants.MSG_TYPE_REQUEST_VOTE_RPC, bytes)
        }
    }

    private fun writeMessage(out: ByteBuf, messageType: Int, bytes: ByteArray) {
        out.writeInt(messageType)
        writeBytes(out, bytes)
    }

    private fun writeBytes(out: ByteBuf, bytes: ByteArray) {
        out.writeInt(bytes.size)
        out.writeBytes(bytes)
    }
}
