package com.qianlei.support

import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import java.io.ByteArrayInputStream
import java.io.InputStream


/**
 * 使用 ByteArray 模拟的SeekableFile，用于测试
 *
 * @author qianlei
 */
class ByteArraySeekableFile(private var content: ByteArray = ByteArray(0)) : SeekableFile {

    private var size = content.size
    private var position = 0

    override fun seek(position: Long) {
        checkPosition(position)
        this.position = position.toInt()
    }

    private fun checkPosition(position: Long) {
        require(!(position < 0 || position > size)) { "offset < 0 or offset > size" }
    }

    override fun writeInt(i: Int) {
        write(Ints.toByteArray(i))
    }

    private fun ensureCapacity(capacity: Int) {
        val oldLength = content.size
        if (position + capacity <= oldLength) {
            return
        }
        if (oldLength == 0) {
            content = ByteArray(capacity)
            return
        }
        val newLength = if (oldLength >= capacity) oldLength * 2 else oldLength + capacity
        val newContent = ByteArray(newLength)
        System.arraycopy(content, 0, newContent, 0, oldLength)
        content = newContent
    }

    override fun writeLong(l: Long) {
        write(Longs.toByteArray(l))
    }

    override fun write(b: ByteArray) {
        val n = b.size
        ensureCapacity(n)
        System.arraycopy(b, 0, content, position, n)
        size = Math.max(position + n, size)
        position += n
    }

    override fun readInt(): Int {
        val buffer = ByteArray(4)
        read(buffer)
        return Ints.fromByteArray(buffer)
    }

    override fun readLong(): Long {
        val buffer = ByteArray(8)
        read(buffer)
        return Longs.fromByteArray(buffer)
    }

    override fun read(b: ByteArray): Int {
        val n = Math.min(b.size, size - position)
        if (n > 0) {
            System.arraycopy(content, position, b, 0, n)
            position += n
        }
        return n
    }

    override fun size(): Long {
        return size.toLong()
    }

    override fun truncate(size: Long) {
        require(size >= 0) { "size < 0" }
        this.size = size.toInt()
        if (position > this.size) {
            position = this.size
        }
    }

    override fun inputStream(start: Long): InputStream {
        checkPosition(start)
        return ByteArrayInputStream(content, start.toInt(), (size - start).toInt())
    }

    override fun position(): Long {
        return position.toLong()
    }

    override fun flush() {}

    override fun close() {}

}
