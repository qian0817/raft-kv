package com.qianlei.log.statemachine

import com.qianlei.log.snapshot.Snapshot
import java.io.OutputStream

class EmptyStateMachine : StateMachine {
    override var lastApplied = 0
        private set

    override fun applyLog(
        context: StateMachineContext,
        index: Int,
        commandBytes: ByteArray,
        firstLogIndex: Int
    ) {
        lastApplied = index
    }

    override fun shouldGenerateSnapshot(firstLogIndex: Int, lastApplied: Int): Boolean {
        return false
    }

    override fun generateSnapshot(output: OutputStream) {
    }

    override fun applySnapshot(snapshot: Snapshot) {
        lastApplied = snapshot.lastIncludeIndex
    }

    override fun shutdown() {}
}