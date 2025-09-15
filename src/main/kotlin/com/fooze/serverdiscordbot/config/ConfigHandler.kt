package com.fooze.serverdiscordbot.config

import com.fooze.serverdiscordbot.ServerDiscordBot
import kotlinx.serialization.json.*
import java.io.File

// TODO: Write a separate config for messages

object ConfigHandler {
    private val configFile = File("config/${ServerDiscordBot.MOD_ID}.json")

    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    var config: ModConfig = ModConfig()
        private set

    fun load() {
        if (configFile.exists()) {
            val loaded = json.parseToJsonElement(configFile.readText()).jsonObject
            val default = json.encodeToJsonElement(ModConfig()).jsonObject
            val merged = JsonObject(default + loaded)
            config = json.decodeFromJsonElement(merged)
            save()
        } else {
            save()
        }
    }

    fun save() {
        configFile.parentFile.mkdirs()
        configFile.writeText(json.encodeToString(config))
    }
}