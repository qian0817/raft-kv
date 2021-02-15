package com.qianlei.rpc.nio

import com.qianlei.node.NodeId
import com.qianlei.rpc.message.*
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_APPEND_ENTRIES_RESULT
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_APPEND_ENTRIES_RPC
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_INSTALL_SNAPSHOT_RESULT
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_INSTALL_SNAPSHOT_RPC
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_NODE_ID
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_REQUEST_VOTE_RESULT
import com.qianlei.rpc.message.MessageConstants.Companion.MSG_TYPE_REQUEST_VOTE_RPC
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 *
 * @author qianlei
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class Decoder : ByteToMessageDecoder() {
    public override fun decode(ctx: ChannelHandlerContext?, byteBuf: ByteBuf, out: MutableList<Any>) {
        val available = byteBuf.readableBytes()
        if (available < 8) {
            return
        }
        byteBuf.markReaderIndex()
        val messageType = byteBuf.readInt()
        val payLoadLength = byteBuf.readInt()
        // 消息未完全可读
        if (byteBuf.readableBytes() < payLoadLength) {
            byteBuf.resetReaderIndex()
            return
        }
        val payLoad = ByteArray(payLoadLength)
        byteBuf.readBytes(payLoad)
        try {
            when (messageType) {
                MSG_TYPE_NODE_ID -> out.add(NodeId(String(payLoad)))
                MSG_TYPE_REQUEST_VOTE_RPC -> out.add(ProtoBuf.decodeFromByteArray<RequestVoteRpc>(payLoad))
                MSG_TYPE_REQUEST_VOTE_RESULT -> out.add(ProtoBuf.decodeFromByteArray<RequestVoteResult>(payLoad))
                MSG_TYPE_APPEND_ENTRIES_RESULT -> out.add(ProtoBuf.decodeFromByteArray<AppendEntriesResult>(payLoad))
                MSG_TYPE_APPEND_ENTRIES_RPC -> out.add(ProtoBuf.decodeFromByteArray<AppendEntriesRpc>(payLoad))
                MSG_TYPE_INSTALL_SNAPSHOT_RESULT -> out.add(ProtoBuf.decodeFromByteArray<InstallSnapshotResult>(payLoad))
                MSG_TYPE_INSTALL_SNAPSHOT_RPC -> out.add(ProtoBuf.decodeFromByteArray<InstallSnapshotRpc>(payLoad))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}