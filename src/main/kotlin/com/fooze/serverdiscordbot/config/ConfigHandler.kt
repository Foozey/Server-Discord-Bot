package com.fooze.serverdiscordbot.config

import com.fooze.serverdiscordbot.ServerDiscordBot.MOD_ID
import kotlinx.serialization.json.*
import java.io.File

object ConfigHandler {
    var config: ModConfig = ModConfig()
    var lang: LangConfig = LangConfig()
    private val configFile = File("config/$MOD_ID.json")

    // JSON serializer
    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // Loads the config and language files
    fun load() {
        val configText = if (configFile.exists()) configFile.readText() else null
        config = loadMerged(configText, ModConfig())
        lang = loadLang()
        save()
    }

    // Saves the config file
    private fun save() {
        configFile.parentFile.mkdirs()
        configFile.writeText(json.encodeToString(config))
    }

    // Merges the loaded JSON with the default values
    private inline fun <reified T> loadMerged(jsonText: String?, defaultConfig: T): T {
        if (jsonText == null) return defaultConfig
        val loaded = json.parseToJsonElement(jsonText).jsonObject
        val defaults = json.encodeToJsonElement(defaultConfig).jsonObject
        return json.decodeFromJsonElement(JsonObject(defaults + loaded))
    }

    // Loads the language file from the mod resources
    private fun loadLang(): LangConfig {
        val path = "/assets/$MOD_ID/lang/${config.language}.json"
        val stream = javaClass.getResourceAsStream(path) ?: return LangConfig()
        return loadMerged(stream.reader().readText(), LangConfig())
    }
}