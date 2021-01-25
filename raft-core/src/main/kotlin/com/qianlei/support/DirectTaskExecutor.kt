package com.qianlei.support

import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

/**
 * 直接执行版本的任务执行器，方便测试使用
 * @author qianlei
 */
class DirectTaskExecutor : TaskExecutor {
    override fun submit(task: Runnable): Future<*> {
        return FutureTask(task, null).apply { run() }
    }

    override fun <V> submit(task: Callable<V>): Future<V> {
        return FutureTask(task).apply { run() }
    }

    override fun shutdown() {}
}