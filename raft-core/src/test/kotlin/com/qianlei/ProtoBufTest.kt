package com.qianlei

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
        val map = mapOf("1" to "1", "2" to "2", "3" to "3")
        val toByteArray = ProtoBuf.encodeToByteArray(map)
        assertEquals(map, ProtoBuf.decodeFromByteArray(toByteArray))
//        val rpc = InstallSnapshotRpc(
//            1,
//            NodeId.of("A"),
//            1,
//            1,
//            1,
//            "abcdefg".encodeToByteArray(),
//            true
//        )
//        val bytes = ProtoBuf.encodeToByteArray(rpc)
//        val decodeRpc = ProtoBuf.decodeFromByteArray<InstallSnapshotRpc>(bytes)
//        assertEquals(rpc, decodeRpc)
    }
}