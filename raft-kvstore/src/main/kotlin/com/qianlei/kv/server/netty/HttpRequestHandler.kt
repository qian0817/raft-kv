package com.qianlei.kv.server.netty

import com.qianlei.kv.server.Service
import com.qianlei.node.role.RoleName
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*


/**
 *
 * @author qianlei
 */
class HttpRequestHandler(
    private val service: Service
) : SimpleChannelInboundHandler<FullHttpRequest>() {
    override fun channelRead0(ctx: ChannelHandlerContext, req: FullHttpRequest) {
        // 获取请求的uri
        when {
            req.match("/", HttpMethod.GET) -> getClusterInfo()
            req.match("/data/", HttpMethod.GET) -> doGetAll()
            req.match("/data/{key}", HttpMethod.GET) -> {
                val key = req.getPathVariable("/data/{key}", "key")!!
                doGet(key)
            }
            req.match("/data/{key}/{value}", HttpMethod.GET) -> {
                val key = req.getPathVariable("/data/{key}/{value}", "key")!!
                val value = req.getPathVariable("/data/{key}/{value}", "value")!!
                doSet(key, value, ctx)
            }
            else -> buildResponse("path not found", HttpResponseStatus.NOT_FOUND)
        }?.let { response ->
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
        }
    }

    private fun getClusterInfo(): HttpResponse {
        return buildResponse(mapOf("name" to "raft-kv"))
    }

    private fun doSet(key: String, value: String, ctx: ChannelHandlerContext): HttpResponse? {
        val nodeState = service.getNodeState()
        if (nodeState.role != RoleName.LEADER) {
            return buildResponse(mapOf("message" to "not leader"))
        }
        service.set(key, value, ctx)
        return null
    }

    private fun doGetAll(): HttpResponse {
        val allData = service.getAll()
        return buildResponse(allData)
    }

    private fun doGet(key: String): HttpResponse {
        val value = service.get(key)
        return buildResponse(mapOf("key" to key, "value" to value))
    }
}