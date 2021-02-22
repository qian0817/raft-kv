package com.qianlei.kv.server.config

import com.charleskorn.kaml.Yaml
import com.qianlei.kv.server.config.ConfigFactory.JSON
import com.qianlei.kv.server.config.ConfigFactory.YAML
import com.qianlei.node.NodeId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigFactoryTest {

    @Test
    fun parseJson() {
        val config = ServerConfig(2333, listOf(), NodeId.of("A"))
        val content = Json.encodeToString(config)
        assertEquals(config, ConfigFactory.parseConfig(content, JSON))
    }

    @Test
    fun parseYaml() {
        val config = ServerConfig(2333, listOf(), NodeId.of("A"))
        val content = Yaml.default.encodeToString(config)
        assertEquals(config, ConfigFactory.parseConfig(content, YAML))
    }

}