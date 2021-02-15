package com.qianlei.log.entry

import kotlinx.serialization.Serializable

@Serializable
data class EntryList(
    val entryList: List<Pair<String, String>>
)
