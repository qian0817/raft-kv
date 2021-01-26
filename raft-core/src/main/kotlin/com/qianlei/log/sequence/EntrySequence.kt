package com.qianlei.log.sequence

import com.qianlei.log.entry.Entry
import com.qianlei.log.entry.EntryMeta

/**
 * 日志序列
 * 将日志组件拆分为日志序列，将序列部分抽象出来，是为了方便加入日志快照机制
 *
 * @author qianlei
 */
interface EntrySequence {
    /**
     * 日志序列是否为空
     */
    fun isEmpty(): Boolean

    /**
     * 获取第一条日志的索引
     */
    val firstLogIndex: Int

    /**
     * 获取最后一条日志的索引
     */
    val lastLogIndex: Int

    /**
     * 获取下一条日志的索引
     */
    val nextLogIndex: Int

    /**
     * 获取序列的子视图，从[fromIndex]开始
     */
    fun subList(fromIndex: Int): List<Entry>

    /**
     * 获取序列的子视图从[fromIndex]到[toIndex]
     */
    fun subList(fromIndex: Int, toIndex: Int): List<Entry>

    /**
     * 检查某个日志条目是否存在
     */
    fun isEntryPresent(index: Int): Boolean

    /**
     * 获取某个日志条目的元信息
     */
    fun getEntryMeta(index: Int): EntryMeta?

    /**
     * 获取某个日志条目
     */
    fun getEntry(index: Int): Entry?

    /**
     * 最后一个日志条目
     */
    val lastEntry: Entry?

    /**
     * 追加日志
     */
    fun append(entry: Entry)

    /**
     * 追加多条日志
     */
    fun append(entries: List<Entry>)

    /**
     * 推进 commitIndex
     */
    fun commit(index: Int)

    /**
     * 获取当前的 commitIndex
     */
    val commitIndex: Int

    /**
     * 移除某个索引之后的日志条目
     * 用于在追加来自Leader节点的日志出现日志冲突的情况下，移除现有日志
     */
    fun removeAfter(index: Int)

    /**
     * 关闭日志序列
     */
    fun close()
}