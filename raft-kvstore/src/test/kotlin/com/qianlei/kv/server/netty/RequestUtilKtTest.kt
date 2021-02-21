package com.qianlei.kv.server.netty

import io.netty.handler.codec.http.DefaultHttpRequest
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpVersion
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class RequestUtilKtTest {
    private fun buildRequest(url: String, method: HttpMethod = HttpMethod.GET) =
        DefaultHttpRequest(HttpVersion.HTTP_1_1, method, url)

    @Test
    fun testMatch() {
        assertTrue(buildRequest("/data/1").match("/data/{id}/"))
        assertTrue(buildRequest("/data/1/").match("/data/{id}/"))
        assertFalse(buildRequest("/datas/1").match("/data/{id}/"))

        assertTrue(buildRequest("/data/1").match("/data/{id}"))
        assertTrue(buildRequest("/data/1/").match("/data/{id}"))
        assertFalse(buildRequest("/datas/1").match("/data/{id}"))

        assertTrue(buildRequest("/data/1").match("/data/{id}", HttpMethod.GET))
        assertFalse(buildRequest("/data/1/").match("/data/{id}", HttpMethod.POST))
    }

    @Test
    fun testGetPathVariable() {
        assertEquals(buildRequest("/data/1").getPathVariable("/data/{id}/", "id"), "1")
        assertEquals(buildRequest("/data/1").getPathVariable("/data/{id}", "id"), "1")
        assertEquals(buildRequest("/data/1").getPathVariable("/data/{id}", "key"), null)
        assertEquals(buildRequest("/data/1").getPathVariable("/datas/{id}", "id"), null)
    }

}