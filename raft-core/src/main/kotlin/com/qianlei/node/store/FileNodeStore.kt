package com.qianlei.node.store

import com.qianlei.node.NodeId
import com.qianlei.support.RandomAccessFileAdapter
import com.qianlei.support.SeekableFile
import java.io.File
import java.nio.file.Files

/**
 * 基于文件的持久化实现
 * 文件格式如下
 * 1. 4字节 currentTerm
 * 2. 4字节 votedFor 长度
 * 3. 变长字节 votedFor 内容
 *
 * @author qianlei
 */
class FileNodeStore : NodeStore {
    private val seekableFile: SeekableFile

    companion object {
        /**
         * 文件名
         */
        const val FILE_NAME = "node.bin"
        const val OFFSET_TERM = 0L
        const val OFFSET_VOTED_FOR = 4L
    }

    init {
        initializeOrLoad()
    }

    constructor(file: File) {
        // 判断文件存在，不存在则创建文件
        if (!file.exists()) {
            Files.createFile(file.toPath())
        }
        seekableFile = RandomAccessFileAdapter(file)
    }

    constructor(seekableFile: SeekableFile) {
        this.seekableFile = seekableFile
    }

    private fun initializeOrLoad() {
        if (seekableFile.size() == 0L) {
            // 进行初始化文件
            seekableFile.truncate(8)
            seekableFile.seek(0)
            seekableFile.writeInt(0)
            seekableFile.writeInt(0)
        } else {
            // 加载读取 term
            term = seekableFile.readInt()
            // 读取 votedFor 内容
            val length = seekableFile.readInt()
            if (length > 0) {
                val bytes = ByteArray(length)
                seekableFile.read(bytes)
                votedFor = NodeId(String(bytes))
            }
        }
    }

    override var term: Int = 0
        set(value) {
            seekableFile.seek(OFFSET_TERM)
            seekableFile.writeInt(value)
            field = value
        }

    override var votedFor: NodeId? = null
        set(value) {
            seekableFile.seek(OFFSET_VOTED_FOR)
            if (field == null) {
                seekableFile.writeInt(0)
                seekableFile.truncate(8L)
            } else {
                val bytes = field!!.value.encodeToByteArray()
                seekableFile.writeInt(bytes.size)
                seekableFile.write(bytes)
            }
            field = value
        }

    override fun close() = seekableFile.close()
}