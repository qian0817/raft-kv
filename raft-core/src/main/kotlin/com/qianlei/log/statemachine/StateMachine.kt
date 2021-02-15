package com.qianlei.log.statemachine

import com.qianlei.log.snapshot.Snapshot
import java.io.OutputStream

/**
 *
 * @author qianlei
 */
interface StateMachine {
    fun getLastApplied(): Int
    fun applyLog(context: StateMachineContext, index: Int, commandBytes: ByteArray, firstLogIndex: Int)
    fun shouldGenerateSnapshot(firstLogIndex: Int, lastApplied: Int): Boolean
    fun generateSnapshot(output: OutputStream)
    fun applySnapshot(snapshot: Snapshot)
    fun shutdown()
}