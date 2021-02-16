package com.qianlei.kv.server.netty

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun HttpRequest.match(url: String, method: HttpMethod): Boolean {
    return method == method() && match(url)
}

fun HttpRequest.match(url: String): Boolean {
    val realUrl = if (uri().endsWith("/")) uri().substring(0, uri().length - 1) else uri()
    val mappingUrl = if (url.endsWith("/")) url.substring(0, url.length - 1) else url
    val realUrlSplit = realUrl.split("/")
    val mappingUrlSplit = mappingUrl.split("/")
    if (realUrlSplit.size != mappingUrlSplit.size) {
        return false
    }
    for (split in realUrlSplit.indices) {
        if (mappingUrlSplit[split].startsWith("{") && mappingUrlSplit[split].endsWith("}")) {
            continue
        }
        if (mappingUrlSplit[split] != realUrlSplit[split]) {
            return false
        }
    }
    return true
}

fun HttpRequest.getPathVariable(mappingUrl: String, name: String): String? {
    if (!match(mappingUrl)) {
        return null
    }
    val urlSplit = uri().split("/")
    val mappingUrlSplit = mappingUrl.split("/")
    for (i in mappingUrlSplit.indices) {
        if (mappingUrlSplit[i] == "{$name}") {
            return when {
                urlSplit[i].contains("?") -> urlSplit[i].split("[?]").toTypedArray()[0]
                urlSplit[i].contains("&") -> urlSplit[i].split("&").toTypedArray()[0]
                else -> urlSplit[i]
            }
        }
    }
    return null
}

inline fun <reified T> FullHttpRequest.parseBody(): T {
    val size = content().readableBytes()
    val content = ByteArray(size)
    content().readBytes(content)
    return Json.decodeFromString(content.decodeToString())
}