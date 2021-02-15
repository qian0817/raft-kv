package com.qianlei.log.statemachine

interface StateMachineContext {
    fun generateSnapshot(lastIncludedIndex: Int)
}