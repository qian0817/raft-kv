package com.qianlei.rpc.message

/**
 *
 * @author qianlei
 */
interface MessageConstants {
    companion object {
        const val MSG_TYPE_NODE_ID = 0
        const val MSG_TYPE_REQUEST_VOTE_RPC = 1
        const val MSG_TYPE_REQUEST_VOTE_RESULT = 2
        const val MSG_TYPE_APPEND_ENTRIES_RPC = 3
        const val MSG_TYPE_APPEND_ENTRIES_RESULT = 4
        const val MSG_TYPE_INSTALL_SNAPSHOT_RPC = 5
        const val MSG_TYPE_INSTALL_SNAPSHOT_RESULT = 6
    }
}