package com.fooze.serverdiscordbot.config

import kotlinx.serialization.json.Json
import java.io.File

object ConfigHandler {
    private val configFile = File("config/server-discord-bot.json")

    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
    }

    var config: ModConfig = ModConfig()

    fun load() {
       if (configFile.exists()) {
           config = json.decodeFromString(configFile.readText())
       } else {
           save()
       }
    }

    fun save() {
        configFile.parentFile.mkdirs()
        configFile.writeText(json.encodeToString(config))
    }
}