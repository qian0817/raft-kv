package com.qianlei.log

/**
 *
 * @author qianlei
 */
open class LogException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable) : super(message, cause)
}