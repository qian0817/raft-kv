package com.qianlei.kv

import com.qianlei.kv.message.*
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_FAILURE
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_GET_COMMAND
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_GET_COMMAND_RESPONSE
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_REDIRECT
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_SET_COMMAND
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_SUCCESS
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 *
 * @author qianlei
 */
class CommonDecoder : ByteToMessageDecoder() {
    @Suppress("EXPERIMENTAL_API_USAGE")
    override fun decode(ctx: ChannelHandlerContext?, byteBuf: ByteBuf, out: MutableList<Any>) {
        if (byteBuf.readableBytes() < 8) {
            return
        }
        byteBuf.markReaderIndex()
        val type = byteBuf.readInt()
        val length = byteBuf.readInt()
        if (byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex()
            return
        }
        val payload = ByteArray(length)
        byteBuf.readBytes(payload)
        when (type) {
            MSG_TYPE_SUCCESS -> out.add(Success)
            MSG_TYPE_FAILURE -> out.add(ProtoBuf.decodeFromByteArray<Failure>(payload))
            MSG_TYPE_REDIRECT -> out.add(ProtoBuf.decodeFromByteArray<Redirect>(payload))
            MSG_TYPE_GET_COMMAND -> out.add(ProtoBuf.decodeFromByteArray<GetCommand>(payload))
            MSG_TYPE_GET_COMMAND_RESPONSE -> out.add(ProtoBuf.decodeFromByteArray<GetCommandResponse>(payload))
            MSG_TYPE_SET_COMMAND -> out.add(ProtoBuf.decodeFromByteArray<SetCommand>(payload))
        }
    }
}