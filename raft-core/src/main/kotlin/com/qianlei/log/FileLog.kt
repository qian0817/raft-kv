package com.qianlei.log

import com.google.common.eventbus.EventBus
import com.qianlei.log.entry.EntryMeta
import com.qianlei.log.sequence.FileEntrySequence
import com.qianlei.log.snapshot.*
import com.qianlei.node.NodeEndpoint
import com.qianlei.rpc.message.InstallSnapshotRpc
import java.io.File
import kotlin.math.max

/**
 *
 * @author qianlei
 */
@Suppress("UnstableApiUsage")
class FileLog(baseDir: File, eventBus: EventBus) : AbstractLog(eventBus) {
    private val rootDir = RootDir(baseDir)

    init {
        val latestGeneration = rootDir.getLatestGeneration()
        snapshot = EmptySnapshot()
        if (latestGeneration != null) {
            if (latestGeneration.getSnapshotFile().exists()) {
                snapshot = FileSnapshot(latestGeneration)
            }
            val fileEntrySequence = FileEntrySequence(latestGeneration, latestGeneration.lastIncludedIndex)
            commitIndex = fileEntrySequence.commitIndex
            entrySequence = fileEntrySequence
        } else {
            val firstGeneration = rootDir.createFirstGeneration()
            entrySequence = FileEntrySequence(firstGeneration, 1)
        }
    }

    override fun generateSnapshot(lastAppliedEntryMeta: EntryMeta, groupConfig: List<NodeEndpoint>): Snapshot {
        val logDir = rootDir.getLogDirForGenerating()
        FileSnapshotWriter(
            logDir.getSnapshotFile(),
            lastAppliedEntryMeta.index,
            lastAppliedEntryMeta.term
        ).use { stateMachine.generateSnapshot(it.output) }
        return FileSnapshot(logDir)

    }

    override fun newSnapshotBuilder(firstRpc: InstallSnapshotRpc): SnapshotBuilder<out Snapshot> {
        return FileSnapshotBuilder(firstRpc, rootDir.getLogDirForInstalling())
    }

    override fun replaceSnapshot(newSnapshot: Snapshot) {
        val fileSnapshot = newSnapshot as FileSnapshot
        val lastIncludeIndex = fileSnapshot.lastIncludeIndex
        val logIndexOffset = lastIncludeIndex + 1

        val remainingEntries = entrySequence.subList(logIndexOffset)
        val newEntrySequence = FileEntrySequence(fileSnapshot.logDir, logIndexOffset)
        newEntrySequence.append(remainingEntries)
        newEntrySequence.commit(max(entrySequence.commitIndex, lastIncludeIndex))
        newEntrySequence.close()

        snapshot.close()
        entrySequence.close()
        newSnapshot.close()

        val generation = rootDir.rename(fileSnapshot.logDir, lastIncludeIndex)
        snapshot = FileSnapshot(generation)
        entrySequence = FileEntrySequence(generation, logIndexOffset)
        commitIndex = entrySequence.commitIndex
    }
}