package com.qianlei.kv.server.netty

import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun buildResponse(content: String, status: HttpResponseStatus = HttpResponseStatus.OK): HttpResponse {
    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(content.encodeToByteArray()))
}

inline fun <reified T> buildResponse(content: T, status: HttpResponseStatus = HttpResponseStatus.OK): HttpResponse {
    return buildResponse(Json.encodeToString(content), status)
}