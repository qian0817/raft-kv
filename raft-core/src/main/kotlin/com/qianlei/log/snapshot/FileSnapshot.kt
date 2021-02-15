package com.qianlei.log.snapshot

import com.qianlei.log.LogDir
import com.qianlei.support.RandomAccessFileAdapter
import com.qianlei.support.SeekableFile
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.File
import java.io.InputStream
import kotlin.math.min

/**
 *
 * @author qianlei
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class FileSnapshot(
    private val seekableFile: SeekableFile,
) : Snapshot {
    constructor(logDir: LogDir) : this(logDir.getSnapshotFile()) {
        this.logDir = logDir
    }

    constructor(file: File) : this(RandomAccessFileAdapter(file, "r"))

    lateinit var logDir: LogDir
    private val headerLength: Int
    private val dataStart: Long
    override val lastIncludeIndex: Int
    override val lastIncludeTerm: Int
    override val dataSize: Long

    init {
        headerLength = seekableFile.readInt()
        val headerBytes = ByteArray(headerLength)
        seekableFile.read(headerBytes)
        val header = ProtoBuf.decodeFromByteArray<SnapshotHeader>(headerBytes)
        lastIncludeIndex = header.lastIndex
        lastIncludeTerm = header.lastTerm
        dataStart = seekableFile.position()
        dataSize = seekableFile.size() - dataStart
    }


    override fun readData(offset: Int, length: Int): SnapshotChunk {
        check(offset <= dataSize) { "offset > data size" }
        seekableFile.seek(dataStart + offset)
        val size = min(length, (dataSize - offset).toInt())
        val buffer = ByteArray(size)
        val n = seekableFile.read(buffer)
        return SnapshotChunk(buffer, offset + n >= dataSize)
    }

    override val dataStream: InputStream
        get() = seekableFile.inputStream(dataStart)

    override fun close() {
        seekableFile.close()
    }
}