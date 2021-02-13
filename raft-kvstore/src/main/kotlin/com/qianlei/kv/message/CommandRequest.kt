package com.qianlei.kv.message

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 *
 * @author qianlei
 */
class CommandRequest<T>(val command: T, private val call: ApplicationCall) {
    fun reply(response: Any) {
        GlobalScope.launch {
            when (response) {
                is Success -> call.respondText("ok")
                is Failure -> call.respondText(Json.encodeToString(response), ContentType.Application.Json)
                is GetCommandResponse -> call.respondText(Json.encodeToString(response), ContentType.Application.Json)
                is Redirect -> call.respondText(Json.encodeToString(response), ContentType.Application.Json)
            }
        }
    }
}