package com.qianlei.node.statemachine

import com.qianlei.log.entry.Entry

/**
 *
 * @author qianlei
 */
interface StateMachine {

    val lastApplied: Int

    fun applyLog(context: StateMachineContext, index: Int, commandBytes: ByteArray, firstLogIndex: Int)

    fun applyEntry(entry: Entry)

    fun shutdown()
}