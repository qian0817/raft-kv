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
            throw IOException("failed to create directory $dir")
        }
        getEntriesFile().createNewFile()
        getEntryOffsetIndexFile().createNewFile()
    }

    override fun exists(): Boolean {
        return dir.exists()
    }

    override fun getSnapshotFile(): File {
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