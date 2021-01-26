package com.qianlei.support

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.RandomAccessFile

/**
 * 将 RandomAccessFile 转换为 SeekableFile 的适配器
 *
 * @author
 */
class RandomAccessFileAdapter(private val file: File, mode: String = "rw") : SeekableFile {
    private var randomAccessFile = RandomAccessFile(file, mode)

    override fun seek(position: Long) {
        randomAccessFile.seek(position)
    }

    override fun writeInt(i: Int) {
        randomAccessFile.writeInt(i)
    }

    override fun writeLong(l: Long) {
        randomAccessFile.writeLong(l)
    }

    override fun write(b: ByteArray) {
        randomAccessFile.write(b)
    }

    override fun readInt(): Int {
        return randomAccessFile.readInt()
    }

    override fun readLong(): Long {
        return randomAccessFile.readLong()
    }

    override fun read(b: ByteArray): Int {
        return randomAccessFile.read(b)
    }

    override fun size(): Long {
        return randomAccessFile.length()
    }

    override fun truncate(size: Long) {
        randomAccessFile.setLength(size)
    }

    override fun inputStream(start: Long): InputStream {
        val input = FileInputStream(file)
        if (start > 0) {
            input.skip(start)
        }
        return input
    }

    override fun position(): Long {
        return randomAccessFile.filePointer
    }

    override fun flush() {
    }

    override fun close() {
        randomAccessFile.close()
    }
}