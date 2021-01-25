package com.qianlei.support

import java.io.InputStream

interface SeekableFile {
    fun position(): Long

    fun seek(position: Long)

    fun writeInt(i: Int)

    fun writeLong(l: Long)

    fun write(b: ByteArray)

    fun readInt(): Int

    fun readLong(): Long

    fun read(b: ByteArray): Int

    fun size(): Long

    fun truncate(size: Long)

    fun inputStream(start: Long): InputStream

    fun flush()

    fun close()
}