package com.qianlei.log.entry

interface Entry {
    companion object {
        const val KIND_NO_OP = 0
        const val KIND_GENERAL = 1
        const val KIND_ADD_NODE = 3
        const val KIND_REMOVE_NODE = 4
    }

    fun getKind(): Int

    fun getIndex(): Int

    fun getTerm(): Int

    fun getMeta(): EntryMeta

    fun getCommandBytes(): ByteArray
}