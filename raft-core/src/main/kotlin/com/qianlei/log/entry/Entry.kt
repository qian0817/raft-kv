package com.qianlei.log.entry

/**
 * 日志接口
 * 日志主要有一下四种
 * 1. 普通日志，负载为上层服务的操作
 * 2. NO_OP日志，选举产生的新 leader 节点增加的第一条空日志
 * 3.
 * 4.
 *
 * @author qianlei
 */
interface Entry {
    companion object {
        const val KIND_NO_OP = 0
        const val KIND_GENERAL = 1
        const val KIND_ADD_NODE = 3
        const val KIND_REMOVE_NODE = 4
    }

    /**
     * 日志类型
     */
    val kind: Int

    /**
     * 日志索引
     */
    val index: Int

    val term: Int

    /**
     * 日志元信息
     */
    val meta: EntryMeta

    /**
     * 日志负载
     */
    val commandBytes: ByteArray
}