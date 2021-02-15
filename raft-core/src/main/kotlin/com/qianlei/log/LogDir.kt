package com.qianlei.log

import java.io.File

/**
 *
 * @author qianlei
 */
interface LogDir {
    /**
     * 初始化目录
     */
    fun initialize()

    /**
     * 是否存在
     */
    fun exists(): Boolean

    /**
     * 获取 EntriesFile 对应的文件
     */
    fun getEntriesFile(): File

    /**
     * 获取 EntryOffsetIndexFile 对应的文件
     */
    fun getEntryOffsetIndexFile(): File

    fun getSnapshotFile(): File

    /**
     * 获取目录
     */
    fun get(): File

    /**
     * 重命名目录
     */
    fun renameTo(logDir: LogDir): Boolean
}