package com.qianlei.rpc.nio

import com.qianlei.node.NodeId
import com.qianlei.rpc.message.AppendEntriesResult
import com.qianlei.rpc.message.AppendEntriesRpc
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_APPEND_ENTRIES_RESULT
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_APPEND_ENTRIES_RPC
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_NODE_ID
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_REQUEST_VOTE_RESULT
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_REQUEST_VOTE_RPC
import com.qianlei.rpc.message.RequestVoteResult
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
        when (msg) {
            is NodeId -> writeMessage(out, MSG_TYPE_NODE_ID, msg.value.encodeToByteArray())
            is RequestVoteRpc -> writeMessage(out, MSG_TYPE_REQUEST_VOTE_RPC, ProtoBuf.encodeToByteArray(msg))
            is RequestVoteResult -> writeMessage(out, MSG_TYPE_REQUEST_VOTE_RESULT, ProtoBuf.encodeToByteArray(msg))
            is AppendEntriesRpc -> writeMessage(out, MSG_TYPE_APPEND_ENTRIES_RPC, ProtoBuf.encodeToByteArray(msg))
            is AppendEntriesResult -> writeMessage(out, MSG_TYPE_APPEND_ENTRIES_RESULT, ProtoBuf.encodeToByteArray(msg))
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
