package com.qianlei

import com.qianlei.node.NodeId
import com.qianlei.rpc.message.InstallSnapshotRpc
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 * @author qianlei
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class ProtoBufTest {
    @Test
    fun testProtoBuf() {
        val toByteArray = ProtoBuf.encodeToByteArray(mapOf("a" to "b"))
        val map = ProtoBuf.decodeFromByteArray<Map<String, String>>(toByteArray)
        assertEquals(map, mapOf("a" to "b"))
        val rpc = InstallSnapshotRpc(
            1,
            NodeId.of("A"),
            1,
            1,
            1,
            "abcdefg".encodeToByteArray(),
            true
        )
        val bytes = ProtoBuf.encodeToByteArray(rpc)
        val decodeRpc = ProtoBuf.decodeFromByteArray<InstallSnapshotRpc>(bytes)
        assertEquals(rpc, decodeRpc)
    }
}