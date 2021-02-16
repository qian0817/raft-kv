package com.qianlei.log.entry

import kotlinx.serialization.Serializable

/**
 * 日志条目
 * 在 Raft 中每个操作都会被当作一个日志条目
 * 所有来自客户端的数据变更操作都会被当作一个日志条目追加到节点日志中去
 * 日志条目种类包括
 * 1. 普通日志，负载为上层服务的操作
 * 2. NO_OP日志，选举产生的新 leader 节点增加的第一条空日志
 *
 * @author qianlei
 */

@Serializable
sealed class Entry(
    val kind: Int,
    val index: Int,
    val term: Int
) {
    companion object {
        const val KIND_NO_OP = 0
        const val KIND_GENERAL = 1
        const val KIND_ADD_NODE = 3
        const val KIND_REMOVE_NODE = 4
    }

    abstract val commandBytes: ByteArray

    val meta: EntryMeta
        get() = EntryMeta(kind, index, term)
}

@Serializable
class NoOpEntry : Entry {
    constructor(index: Int, term: Int) : super(KIND_NO_OP, index, term)

    override val commandBytes: ByteArray
        get() = byteArrayOf()

    override fun toString(): String {
        return "NoOpEntry(index=$index, term=$term)"
    }
}


@Serializable
class GeneralEntry : Entry {
    override val commandBytes: ByteArray

    constructor(
        index: Int,
        term: Int,
        commandBytes: ByteArray
    ) : super(KIND_GENERAL, index, term) {
        this.commandBytes = commandBytes
    }

    override fun toString(): String {
        return "GeneralEntry(index=$index, term=$term)"
    }
}