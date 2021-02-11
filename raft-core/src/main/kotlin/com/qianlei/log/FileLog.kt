package com.qianlei.log

import com.qianlei.log.sequence.FileEntrySequence
import java.io.File

/**
 *
 * @author qianlei
 */
class FileLog(baseDir: File) : AbstractLog() {
    init {
        val rootDir = RootDir(baseDir)
        val latestGeneration = rootDir.getLatestGeneration()
        val entrySequence = if (latestGeneration != null) {
            FileEntrySequence(latestGeneration, latestGeneration.lastIncludedIndex)
        } else {
            val firstGeneration = rootDir.createFirstGeneration()
            FileEntrySequence(firstGeneration, 1)
        }
        this.entrySequence = entrySequence
    }

}