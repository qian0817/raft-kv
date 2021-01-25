package com.qianlei.node

class ReplicatingState(nextIndex: Int = 0, matchIndex: Int = 0) {
    var nextIndex: Int = nextIndex
        private set
    var matchIndex: Int = matchIndex
        private set

    var replicating = false
    var lastReplicatedAt = 0L

    fun backOffNextIndex(): Boolean {
        if (nextIndex > 1) {
            nextIndex--
            return true
        }
        return false
    }

    fun advance(lastEntryIndex: Int): Boolean {
        val result = matchIndex != lastEntryIndex || nextIndex != lastEntryIndex + 1
        matchIndex = lastEntryIndex
        nextIndex = lastEntryIndex + 1
        return result
    }
}