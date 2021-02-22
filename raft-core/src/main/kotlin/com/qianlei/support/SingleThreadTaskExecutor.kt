package com.qianlei.support

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory

/**
 * 单线程实现的任务执行器，调用 Executors.newSingleThreadExecutor 方法实现
 *
 * @see Executors.newSingleThreadExecutor
 * @author qianlei
 */
class SingleThreadTaskExecutor(
    threadFactory: ThreadFactory = Executors.defaultThreadFactory()
) : TaskExecutor {
    constructor(threadName: String) : this({ Thread(it, threadName) })

    private val executorService = Executors.newSingleThreadExecutor(threadFactory)

    override fun submit(task: Runnable): Future<*> {
        return executorService.submit { task.run() }
    }

    override fun <V> submit(task: Callable<V>): Future<V> {
        return executorService.submit(task)
    }

    override fun shutdown() {
        executorService.shutdown()
    }
}