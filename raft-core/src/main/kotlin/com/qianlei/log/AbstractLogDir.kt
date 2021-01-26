package com.qianlei.log

import java.io.File
import java.io.IOException

/**
 *
 * @author qianlei
 */
abstract class AbstractLogDir(protected val dir: File) : LogDir {
    override fun initialize() {
        if (!dir.exists() && !dir.mkdir()) {
            throw LogException("failed to create directory $dir")
        }
        try {
            getEntriesFile().createNewFile()
            getEntryOffsetIndexFile().createNewFile()
        } catch (e: IOException) {
            throw LogException("failed to create file", e)
        }
    }

    override fun exists(): Boolean {
        return dir.exists()
    }

    fun getSnapshotFile(): File {
        return File(dir, RootDir.FILE_NAME_SNAPSHOT)
    }

    override fun getEntriesFile(): File {
        return File(dir, RootDir.FILE_NAME_ENTRIES)
    }

    override fun getEntryOffsetIndexFile(): File {
        return File(dir, RootDir.FILE_NAME_ENTRY_OFFSET_INDEX)
    }

    override fun get(): File {
        return dir
    }

    override fun renameTo(logDir: LogDir): Boolean {
        return dir.renameTo(logDir.get())
    }
}