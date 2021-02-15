package com.qianlei.log

import com.google.common.eventbus.EventBus
import com.qianlei.log.entry.EntryMeta
import com.qianlei.log.sequence.EntrySequence
import com.qianlei.log.sequence.MemoryEntrySequence
import com.qianlei.log.snapshot.MemorySnapshot
import com.qianlei.log.snapshot.MemorySnapshotBuilder
import com.qianlei.log.snapshot.Snapshot
import com.qianlei.log.snapshot.SnapshotBuilder
import com.qianlei.node.NodeEndpoint
import com.qianlei.rpc.message.InstallSnapshotRpc
import mu.KotlinLogging
import java.io.ByteArrayOutputStream

/**
 *
 * @author qianlei
 */
@Suppress("UnstableApiUsage")
class MemoryLog(entrySequence: EntrySequence = MemoryEntrySequence(), eventBus: EventBus) : AbstractLog(eventBus) {
    private val logger = KotlinLogging.logger { }

    init {
        this.entrySequence = entrySequence
    }

    override fun generateSnapshot(lastAppliedEntryMeta: EntryMeta, groupConfig: List<NodeEndpoint>): Snapshot {
        val output = ByteArrayOutputStream()
        stateMachine.generateSnapshot(output)
        return MemorySnapshot(
            lastAppliedEntryMeta.index,
            lastAppliedEntryMeta.term,
            output.toByteArray()
        )
    }

    override fun newSnapshotBuilder(firstRpc: InstallSnapshotRpc): SnapshotBuilder<out Snapshot> {
        return MemorySnapshotBuilder(firstRpc)
    }

    override fun replaceSnapshot(newSnapshot: Snapshot) {
        val logIndexOffset = newSnapshot.lastIncludeIndex + 1
        val newEntrySequence = MemoryEntrySequence(logIndexOffset)
        val remainingEntries = entrySequence.subList(logIndexOffset)
        newEntrySequence.append(remainingEntries)
        logger.debug { "snapshot -> $newSnapshot" }
        snapshot = newSnapshot
        logger.debug { "entry sequence -> $newEntrySequence" }
        entrySequence = newEntrySequence
    }

    override fun close() {
        entrySequence.close()
    }
}