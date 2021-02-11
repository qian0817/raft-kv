package com.qianlei.log.statemachine

/**
 *
 * @author qianlei
 */
interface StateMachine {
    fun getLastApplied(): Int
    fun applyLog(index: Int, commandBytes: ByteArray)
    fun shutdown()
}