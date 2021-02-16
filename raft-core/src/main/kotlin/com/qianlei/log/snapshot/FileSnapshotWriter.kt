package com.qianlei.log.snapshot

import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.*

/**
 *
 * @author qianlei
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class FileSnapshotWriter(
    out: OutputStream,
    lastIncludedIndex: Int,
    lastIncludedTerm: Int
) : Closeable {
    constructor(
        file: File,
        lastIncludedIndex: Int,
        lastIncludedTerm: Int
    ) : this(DataOutputStream(FileOutputStream(file)), lastIncludedIndex, lastIncludedTerm)

    val output: DataOutputStream = DataOutputStream(out)

    init {
        val headerBytes =
            ProtoBuf.encodeToByteArray(SnapshotHeader(lastIncludedIndex, lastIncludedTerm))
        output.writeInt(headerBytes.size)
        output.write(headerBytes)
    }

    fun write(data: ByteArray) {
        output.write(data)
    }

    override fun close() {
        output.close()
    }
}