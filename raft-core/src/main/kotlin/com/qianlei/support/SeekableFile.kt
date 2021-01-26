package com.qianlei.support

import java.io.Closeable
import java.io.InputStream
import java.io.RandomAccessFile

/**
 * 相当于 RandomAccessFile
 * 为方便测试，将其抽象出来
 *
 * @see RandomAccessFile
 * @author qianlei
 */
interface SeekableFile : Closeable {
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
}