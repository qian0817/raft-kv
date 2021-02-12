package com.qianlei.kv.server.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

object ConfigFactory {
    fun parseConfig(filename: String): ServerConfig {
        if (filename.endsWith(".yaml")) {
            val content = Files.readString(Path.of(filename))
            return Yaml.default.decodeFromString(content)
        } else if (filename.endsWith(".json")) {
            val content = Files.readString(Path.of(filename))
            return Json.decodeFromString(content)
        }
        throw IllegalArgumentException("unsupported config type")
    }
}