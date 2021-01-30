package com.qianlei.kv.server

import com.qianlei.kv.message.*
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_FAILURE
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_GET_COMMAND
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_GET_COMMAND_RESPONSE
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_REDIRECT
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_SET_COMMAND
import com.qianlei.kv.message.MessageConstant.Companion.MSG_TYPE_SUCCESS
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
class ServiceEncoder : MessageToByteEncoder<Any>() {
    override fun encode(ctx: ChannelHandlerContext, msg: Any, out: ByteBuf) {
        when (msg) {
            is Success -> writeMessage(MSG_TYPE_SUCCESS, ProtoBuf.encodeToByteArray(Success), out)
            is Failure -> writeMessage(MSG_TYPE_FAILURE, ProtoBuf.encodeToByteArray(msg), out)
            is Redirect -> writeMessage(MSG_TYPE_REDIRECT, ProtoBuf.encodeToByteArray(msg), out)
            is GetCommand -> writeMessage(MSG_TYPE_GET_COMMAND, ProtoBuf.encodeToByteArray(msg), out)
            is GetCommandResponse -> writeMessage(MSG_TYPE_GET_COMMAND_RESPONSE, ProtoBuf.encodeToByteArray(msg), out)
            is SetCommand -> writeMessage(MSG_TYPE_SET_COMMAND, ProtoBuf.encodeToByteArray(msg), out)
        }
    }

    private fun writeMessage(messageType: Int, message: ByteArray, out: ByteBuf) {
        out.writeInt(messageType)
        out.writeInt(message.size)
        out.writeBytes(message)
    }
}