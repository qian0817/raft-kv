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
class RandomAccessFileAdapter(
    private val file: File, mode: String = "rw"
) : SeekableFile, RandomAccessFile(file, mode) {
    override fun flush() {}

    override fun size(): Long = length()

    override fun truncate(size: Long) = setLength(size)

    override fun inputStream(start: Long): InputStream {
        val input = FileInputStream(file)
        if (start > 0) {
            input.skip(start)
        }
        return input
    }

    override fun position(): Long = filePointer
}