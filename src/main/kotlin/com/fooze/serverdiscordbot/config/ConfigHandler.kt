package com.fooze.serverdiscordbot.config

import com.fooze.serverdiscordbot.ServerDiscordBot
import kotlinx.serialization.json.Json
import java.io.File

object ConfigHandler {
    private val configFile = File("config/" + ServerDiscordBot.MOD_ID + ".json")

    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
    }

    var config: ModConfig = ModConfig()

    fun load() {
        if (configFile.exists()) {
            val text = configFile.readText()
            config = json.decodeFromString(ModConfig.serializer(), text)
        } else {
            save()
        }
    }

    fun save() {
        val text = json.encodeToString(ModConfig.serializer(), config)
        configFile.writeText(text)
    }
}