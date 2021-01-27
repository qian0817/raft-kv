package com.qianlei.rpc.nio

import com.qianlei.node.NodeId
import com.qianlei.rpc.message.MessageConstants
import com.qianlei.rpc.message.RequestVoteRpc
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
        when (messageType) {
            MessageConstants.MSG_TYPE_NODE_ID -> out.add(NodeId(String(payLoad)))
            MessageConstants.MSG_TYPE_REQUEST_VOTE_RPC -> out.add(ProtoBuf.decodeFromByteArray<RequestVoteRpc>(payLoad))
        }
    }
}