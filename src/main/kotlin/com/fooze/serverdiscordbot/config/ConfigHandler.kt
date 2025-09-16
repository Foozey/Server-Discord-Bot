package com.fooze.serverdiscordbot.config

import com.fooze.serverdiscordbot.ServerDiscordBot.MOD_ID
import kotlinx.serialization.json.*
import java.io.File

object ConfigHandler {
    var config: ModConfig = ModConfig()
    var lang: LangConfig = LangConfig()

    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val configFile = File("config/${MOD_ID}/config.json")
    private val langFile = File("config/${MOD_ID}/lang.json")

    fun load() {
        config = loadMerged(configFile, ModConfig())
        lang = loadMerged(langFile, LangConfig())
        save()
    }

    private fun save() {
        configFile.parentFile.mkdirs()
        configFile.writeText(json.encodeToString(config))
        langFile.writeText(json.encodeToString(lang))
    }

    // Helper to merge loaded config with defaults
    private inline fun <reified T> loadMerged(file: File, default: T): T {
        return if (file.exists()) {
            val loaded = json.parseToJsonElement(file.readText()).jsonObject
            val defaults = json.encodeToJsonElement(default).jsonObject
            val merged = JsonObject(defaults + loaded)
            json.decodeFromJsonElement<T>(merged)
        } else {
            default
        }
    }
}