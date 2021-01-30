package com.qianlei.kv.message

/**
 *
 * @author qianlei
 */
interface MessageConstant {
    companion object {
        const val MSG_TYPE_SUCCESS = 0
        const val MSG_TYPE_FAILURE = 1
        const val MSG_TYPE_REDIRECT = 2
        const val MSG_TYPE_ADD_SERVER_COMMAND = 10
        const val MSG_TYPE_REMOVE_SERVER_COMMAND = 11
        const val MSG_TYPE_GET_COMMAND = 100
        const val MSG_TYPE_GET_COMMAND_RESPONSE = 101
        const val MSG_TYPE_SET_COMMAND = 102
    }
}