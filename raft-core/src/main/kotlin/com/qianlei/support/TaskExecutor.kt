package com.qianlei.support

import java.util.concurrent.Callable
import java.util.concurrent.Future

/**
 * 抽象的任务执行器
 * @author qianlei
 */
interface TaskExecutor {
    /** 提交任务，无返回值 */
    fun submit(task: Runnable): Future<*>

    /** 提交任务，有返回值 */
    fun <V> submit(task: Callable<V>): Future<V>

    /** 关闭任务执行器 */
    fun shutdown()
}