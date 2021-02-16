package com.qianlei.node

import com.google.common.eventbus.Subscribe
import com.qianlei.log.event.SnapshotGenerateEvent
import com.qianlei.log.snapshot.EntryInSnapshotException
import com.qianlei.log.statemachine.StateMachine
import com.qianlei.node.role.*
import com.qianlei.rpc.message.*
import com.qianlei.schedule.ElectionTimeout
import com.qianlei.schedule.LogReplicationTask
import mu.KotlinLogging
import kotlin.math.min

/**
 * @author qianlei
 */
@Suppress("UnstableApiUsage")
class NodeImpl(
    /** 组件上下文信息 */
    val context: NodeContext
) : Node {
    private val logger = KotlinLogging.logger { }

    /** 节点是否已启动 */
    private var started = false

    /** 当前角色 */
    internal lateinit var role: NodeRole

    @Synchronized
    override fun start() {
        check(!started) { "already start" }
        context.eventBus.register(this)
        context.connector.initialize()
        // 从初始化时
        val store = context.store
        // 启动时为 follower 角色
        changeRole(FollowerNodeRole(store.term, store.votedFor, electionTimeOut = scheduleElectionTimeout()))
        started = true
    }

    override fun getRoleNameAndLeaderId(): RoleNameAndLeaderId {
        val leaderId = role.getLeaderId(context.selfId)
        return RoleNameAndLeaderId(role.name, leaderId)
    }

    /**
     * 处理角色变更,将当前节点的角色变为[newRole]
     */
    private fun changeRole(newRole: NodeRole) {
        logger.debug { "node${context.selfId}, role state changed ${role.name} -> ${newRole.name}" }
        context.store.term = newRole.term
        if (newRole is FollowerNodeRole) {
            context.store.votedFor = newRole.votedFor
        }
        role = newRole
    }

    private fun becomeFollower(
        term: Int,
        votedFor: NodeId?,
        leaderId: NodeId?,
        scheduledElectionTimeout: Boolean
    ) {
        // 取消超时定时器
        role.cancelTimeoutOrTask()
        if (leaderId != null && leaderId == role.getLeaderId(context.selfId)) {
            logger.trace { "current leader is $leaderId ,term $term" }
        }
        //重新创建选举超时定时器或者空定时器
        //ElectionTimeout.NONE 表示不设置选举超时
        val electionTimeout = if (scheduledElectionTimeout) scheduleElectionTimeout() else ElectionTimeout.NONE
        changeRole(FollowerNodeRole(term, votedFor, leaderId, electionTimeout))
    }

    private fun scheduleElectionTimeout() = context.scheduler.scheduleElectionTimeout(::electionTimeout)

    internal fun electionTimeout() {
        context.taskExecutor.submit(::doProcessElectionTimeout)
    }

    /**
     * 处理选举超时的代码
     * 设置选举超时需要做的事情包括变更节点角色以及发送 RequestVote 消息给其他节点
     */
    private fun doProcessElectionTimeout() {
        //对于 Leader 来说不可能发生选举超时
        if (role is LeaderNodeRole) {
            logger.warn { "node ${context.selfId} current role is leader ,is impossible election timeout" }
            return
        }
        val newTerm = role.term + 1
        role.cancelTimeoutOrTask()

        logger.info { "start election, new term is $newTerm" }
        //将自己变成 candidate 角色
        changeRole(CandidateNodeRole(newTerm, scheduleElectionTimeout()))
        val lastEntryMeta = context.log.lastEntryMeta
        //向其他节点发送 requestVote 消息
        val rpc = RequestVoteRpc(newTerm, context.selfId, lastEntryMeta.index, lastEntryMeta.term)
        context.connector.sendRequestVote(rpc, context.group.listEndpointExceptSelf())
    }

    /**
     * 处理接收到 RequestVoteRpcMessage 的信息[rpcMessage]
     */
    @Subscribe
    fun onReceiveRequestVoteRpc(rpcMessage: RequestVoteRpcMessage) {
        context.taskExecutor.submit {
            val endpoint = context.group.findMember(rpcMessage.sourceNodeId).endpoint
            context.connector.replyRequestVote(doProcessRequestVoteRpc(rpcMessage), endpoint)
        }
    }

    /**
     * 处理 requestVoteRpc 消息[message]
     * 需要先比较消息中的 term 于自己的 term，之后根据日志决定是否进行投票
     * 如果对方的 term 比自己小，那么不投票并且返回自己的 term 给对方
     * 如果对方的 term 比自己大，那么将自己切换为 Follower 角色
     * 如果对方的 term 与自己相同，会将自己的角色切换为 Follower
     */
    private fun doProcessRequestVoteRpc(message: RequestVoteRpcMessage): RequestVoteResult {
        val rpc = message.rpc
        // 如果对方的 term 比自己小，那么不投票并且返回自己的 term 给对方
        if (rpc.term < role.term) {
            logger.debug { "term from rpc < current term" }
            return RequestVoteResult(role.term, false)
        }
        // 根据日志决定是否进行投票
        val vote = !context.log.isNewerThan(rpc.lastLogIndex, rpc.lastLogTerm)
        // 如果对方的 term 比自己大，那么将自己切换为 Follower 角色
        if (rpc.term > role.term) {
            becomeFollower(rpc.term, if (vote) rpc.candidateId else null, null, true)
            return RequestVoteResult(rpc.term, vote)
        }
        // 本地的 term 与消息的 term 信息一致
        return when (val role = role) {
            is FollowerNodeRole -> {
                // 在两种情况下投票
                // 1. 自己尚未投过票，并且自己的日志比自己新
                // 2. 自己已经给对方投过票
                // 投票后将自己变为 Follower 角色
                if (role.votedFor == null && vote || role.votedFor == rpc.candidateId) {
                    becomeFollower(role.term, rpc.candidateId, null, true)
                    RequestVoteResult(rpc.term, true)
                } else {
                    RequestVoteResult(rpc.term, false)
                }
            }
            // 候选者以及领导者都不会给别人投票
            is CandidateNodeRole, is LeaderNodeRole -> RequestVoteResult(rpc.term, false)
            else -> throw IllegalStateException("unknown node role ${role.name}")
        }
    }

    /**
     * 处理接收到 RequestVoteResult 消息的行为
     */
    @Subscribe
    fun onReceiveRequestVoteResult(result: RequestVoteResult) {
        context.taskExecutor.submit { doProcessRequestVoteResult(result) }
    }

    /**
     * 收到 RequestVote 响应处理
     * 收到 RequestVote 响应响应以后，需要根据票数决定是否变为 leader
     *
     */
    private fun doProcessRequestVoteResult(result: RequestVoteResult) {
        val role = role
        // 如果对方的 term 比自己大，那么退化为 follower
        if (result.term > role.term) {
            becomeFollower(result.term, null, null, true)
            return
        }
        if (role !is CandidateNodeRole) {
            logger.debug { "receive request vote result and current role is not candidate, ignore" }
            return
        }
        if (!result.voteGranted) {
            return
        }
        val currentVotesCount = role.voteCount + 1
        val countOfMajor = context.group.count()
        logger.debug { "votes count $currentVotesCount, node count $countOfMajor" }
        role.cancelTimeoutOrTask()
        // 如果收到超过一半的票数
        // 就将自己变为 Leader
        if (currentVotesCount > countOfMajor / 2) {
            logger.info { "become leader,term ${role.term}" }
            context.group.resetReplicatingStates(context.log.nextIndex)
            changeRole(LeaderNodeRole(role.term, scheduleLogReplicationTask()))
            // 称为 Leader 后立即发送心跳信息
            context.log.appendEntry(role.term)
        } else {
            changeRole(CandidateNodeRole(role.term, scheduleElectionTimeout(), currentVotesCount))
        }
    }

    private fun scheduleLogReplicationTask(): LogReplicationTask {
        return context.scheduler.scheduleLogReplicationTask(::replicateLog)
    }

    /**
     * 发送心跳消息
     */
    internal fun replicateLog() {
        context.taskExecutor.submit(::doReplicationLog)
    }

    private fun doReplicationLog() {
        logger.debug { "replicate log" }
        context.group.listGroupMemberExceptSelf().forEach {
            if (it.shouldReplicate(900)) {
                doReplicationLog(it)
            } else {
                logger.debug { "node ${it.endpoint.id} is replicating, skip replication task" }
            }
        }
    }

    private fun doReplicationLog(member: GroupMember, maxEntries: Int = -1) {
        member.replicateNow()
        try {
            logger.trace { "replicate log from ${context.selfId} to ${member.endpoint.id}" }
            val rpc = context.log.createAppendEntriesRpc(role.term, context.selfId, member.getNextIndex(), maxEntries)
            context.connector.sendAppendEntries(rpc, member.endpoint)
        } catch (e: EntryInSnapshotException) {
            logger.debug { "log entry ${member.getNextIndex()} in snapshot, replicate with install snapshot RPC" }
            val rpc = context.log.createInstallSnapshotRpc(role.term, context.selfId, 0, 1024)
            context.connector.sendInstallSnapshot(rpc, member.endpoint)
        }
    }

    @Subscribe
    fun onReceiveAppendEntriesRpc(message: AppendEntriesRpcMessage) {
        context.taskExecutor.submit {
            context.connector.replyAppendEntries(
                doProcessEntriesRpc(message),
                context.group.findMember(message.sourceNodeId).endpoint
            )
        }
    }

    /**
     * 非 leader 节点收到 leader 节点的心跳消息以后，需要重置选举超时，记录当前 leader 节点的 id
     */
    private fun doProcessEntriesRpc(message: AppendEntriesRpcMessage): AppendEntriesResult {
        val rpc = message.rpc
        // 如果对方的 term 比自己小，那么回复自己的 term
        if (rpc.term < role.term) {
            return AppendEntriesResult(role.term, false)
        }
        // 如果对象的 term 比自己大，那么退化为 follower 角色
        if (rpc.term > role.term) {
            becomeFollower(rpc.term, null, rpc.leaderId, true)
            return AppendEntriesResult(rpc.term, appendEntries(rpc))
        }
        return when (val role = role) {
            is FollowerNodeRole -> {
                // 设置 leaderId 并且重置选举定时器
                becomeFollower(rpc.term, role.votedFor, rpc.leaderId, true)
                AppendEntriesResult(rpc.term, appendEntries(rpc))
            }
            is CandidateNodeRole -> {
                // 说明有其他的 candidate 成为了 leader
                // 当前节点退化为 follower
                becomeFollower(rpc.term, null, rpc.leaderId, true)
                AppendEntriesResult(rpc.term, appendEntries(rpc))
            }
            is LeaderNodeRole -> {
                logger.warn { "receive append entries rpc from another leader ${rpc.leaderId}" }
                AppendEntriesResult(rpc.term, false)
            }
            else -> throw IllegalStateException("unexpected node role ${role.name}")
        }
    }

    private fun appendEntries(rpc: AppendEntriesRpc): Boolean {
        val result = context.log.appendEntriesFromLeader(rpc.prevLogIndex, rpc.prevLogTerm, rpc.entries)
        if (result) {
            context.log.advanceCommitIndex(min(rpc.leaderCommit, rpc.lastEntryIndex), rpc.term)
        }
        return result
    }

    @Subscribe
    fun onReceiveAppendEntriesResult(message: AppendEntriesResultMessage) {
        context.taskExecutor.submit { doProcessAppendEntriesResult(message) }
    }

    /**
     * 收到 AppendEntries 响应
     */
    private fun doProcessAppendEntriesResult(message: AppendEntriesResultMessage) {
        val result = message.result
        if (result.term > role.term) {
            becomeFollower(result.term, null, null, true)
            return
        }
        if (role !is LeaderNodeRole) {
            logger.warn { "receive append entries from node ${message.sourceNodeId} but current node is not leader" }
        }
        val sourceNodeId = message.sourceNodeId
        val member = context.group.findMemberOrNull(sourceNodeId)
        // 没有指定的成员
        if (member == null) {
            logger.info { "unexpected append entries from node $sourceNodeId, node maybe removed" }
            return
        }
        val rpc = message.rpc
        if (result.success) {
            if (member.advanceReplicatingState(rpc.lastEntryIndex)) {
                context.log.advanceCommitIndex(context.group.getMatchIndexOfMajor(), role.term)
            }
            // node caught up
            if (member.getNextIndex() >= context.log.nextIndex) {
                member.stopReplicating()
                return
            }
        } else {
            if (!member.backOffNextIndex()) {
                logger.warn { "cannot back off next index more,node $sourceNodeId" }
                member.stopReplicating()
                return
            }
        }
        doReplicationLog(member)

    }

    override fun appendLog(commandBytes: ByteArray) {
        context.log.appendEntry(role.term, commandBytes)
        doReplicationLog()
    }

    override fun registerStateMachine(stateMachine: StateMachine) {
        context.log.stateMachine = stateMachine
    }

    @Subscribe
    fun onReceiveInstallSnapshotRpc(rpcMessage: InstallSnapshotRpcMessage) {
        context.taskExecutor.submit {
            context.connector.replyInstallSnapshot(
                doProcessInstallSnapshotRpc(rpcMessage),
                context.group.findMember(rpcMessage.sourceNodeId).endpoint
            )
        }
    }

    private fun doProcessInstallSnapshotRpc(rpcMessage: InstallSnapshotRpcMessage): InstallSnapshotResult {
        val rpc = rpcMessage.rpc
        if (rpc.term < role.term) {
            return InstallSnapshotResult(role.term)
        }
        if (rpc.term > role.term) {
            becomeFollower(rpc.term, null, rpc.leaderId, true)
        }
        context.log.installSnapshot(rpc)
        return InstallSnapshotResult(rpc.term)
    }

    @Subscribe
    fun onReceiveInstallSnapshotResult(resultMessage: InstallSnapshotResultMessage) {
        context.taskExecutor.submit { doProcessInstallSnapshotResult(resultMessage) }
    }

    private fun doProcessInstallSnapshotResult(resultMessage: InstallSnapshotResultMessage) {
        val result = resultMessage.result
        if (result.term > role.term) {
            becomeFollower(result.term, null, null, true)
            return
        }
        if (role !is LeaderNodeRole) {
            logger.warn {
                "receive install snapshot result from node ${resultMessage.sourceNodeId}" +
                        " but current node is not leader, ignore"
            }
            return
        }
        val sourceNodeId: NodeId = resultMessage.sourceNodeId
        val member = context.group.findMemberOrNull(sourceNodeId)
        if (member == null) {
            logger.info { "unexpected install snapshot result from node ${sourceNodeId}, node maybe removed" }
            return
        }

        val rpc = resultMessage.rpc
        if (rpc.done) {
            member.advanceReplicatingState(rpc.lastIndex)
            doReplicationLog(member)
        } else {
            //TODO load from config
            val nextRpc: InstallSnapshotRpc = context.log.createInstallSnapshotRpc(
                role.term, context.selfId,
                rpc.offset + rpc.data.size, 1024
            )
            context.connector.sendInstallSnapshot(nextRpc, member.endpoint)
        }
    }

    @Subscribe
    fun onGenerateSnapshot(event: SnapshotGenerateEvent) {
        context.taskExecutor.submit {
            context.log.generateSnapshot(event.lastIncludedIndex, context.group.listEndpointExceptSelf())
        }
    }

    @Synchronized
    override fun stop() {
        if (!started) {
            throw IllegalStateException("node not started")
        }
        context.scheduler.stop()
        context.connector.close()
        context.taskExecutor.shutdown()
        context.log.close()
        started = false
    }
}