package com.qianlei.kv.server.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

object ConfigFactory {
    const val JSON = "json"
    const val YAML = "yaml"

    fun parseConfig(filename: String): ServerConfig {
        val content = Files.readString(Path.of(filename))
        if (filename.endsWith(".yaml")) {
            return parseConfig(content, YAML)
        }
        return parseConfig(content, JSON)
    }

    fun parseConfig(content: String, type: String): ServerConfig {
        return when (type) {
            JSON -> Json.decodeFromString(content)
            YAML -> Yaml.default.decodeFromString(content)
            else -> throw IllegalArgumentException("unsupported config type")
        }
    }
}