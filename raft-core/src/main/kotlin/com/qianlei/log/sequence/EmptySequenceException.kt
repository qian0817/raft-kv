package com.qianlei.log.sequence

import com.qianlei.log.LogException

/**
 *
 * @author qianlei
 */
class EmptySequenceException : LogException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable) : super(message, cause)
}