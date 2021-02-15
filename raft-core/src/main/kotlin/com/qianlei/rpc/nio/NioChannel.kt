package com.qianlei.rpc.nio

import com.qianlei.rpc.Channel
import com.qianlei.rpc.message.*

/**
 *
 * @author qianlei
 */

class NioChannel(val nettyChannel: io.netty.channel.Channel) : Channel {
    override fun writeRequestVoteRpc(rpc: RequestVoteRpc) {
        nettyChannel.writeAndFlush(rpc)
    }

    override fun writeRequestVoteResult(result: RequestVoteResult) {
        nettyChannel.writeAndFlush(result)
    }

    override fun writeAppendEntriesRpc(rpc: AppendEntriesRpc) {
        nettyChannel.writeAndFlush(rpc)
    }

    override fun writeAppendEntriesResult(result: AppendEntriesResult) {
        nettyChannel.writeAndFlush(result)
    }

    override fun writeInstallSnapshotResult(result: InstallSnapshotResult) {
        nettyChannel.writeAndFlush(result)
    }

    override fun writeInstallSnapshotRpc(rpc: InstallSnapshotRpc) {
        nettyChannel.writeAndFlush(rpc)
    }

    override fun close() {
        nettyChannel.close().sync()
    }
}