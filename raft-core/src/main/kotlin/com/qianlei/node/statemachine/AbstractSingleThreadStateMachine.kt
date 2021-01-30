package com.qianlei.node.statemachine

import com.qianlei.log.entry.Entry
import com.qianlei.support.SingleThreadTaskExecutor
import mu.KotlinLogging

/**
 *
 * @author qianlei
 */
abstract class AbstractSingleThreadStateMachine : StateMachine {
    private val logger = KotlinLogging.logger { }

    @Volatile
    override var lastApplied = 0
        protected set

    private var taskExecutor = SingleThreadTaskExecutor("state-machine")
    override fun applyEntry(entry: Entry) {
        TODO("Not yet implemented")
    }

    override fun applyLog(context: StateMachineContext, index: Int, commandBytes: ByteArray, firstLogIndex: Int) {
        taskExecutor.submit { doApplyLog(context, index, commandBytes, firstLogIndex) }
    }

    private fun doApplyLog(context: StateMachineContext, index: Int, commandBytes: ByteArray, fitstLogIndex: Int) {
        if (index <= lastApplied) {
            return
        }
        logger.debug { "apply log $index" }
        applyCommand(commandBytes)
        lastApplied = index
    }

    protected abstract fun applyCommand(commandBytes: ByteArray)

    override fun shutdown() {
        taskExecutor.shutdown()
    }
}