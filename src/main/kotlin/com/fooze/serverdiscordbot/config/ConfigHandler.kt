package com.fooze.serverdiscordbot.config

import com.fooze.serverdiscordbot.ServerDiscordBot.MOD_ID
import com.fooze.serverdiscordbot.util.Placeholder
import kotlinx.serialization.json.*
import org.slf4j.Logger
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
    fun load(logger: Logger) {
        val loadedConfig = if (configFile.exists()) configFile.readText() else null
        config = loadMerged(loadedConfig, ModConfig())
        lang = loadLang(logger)
        configFile.parentFile.mkdirs()
        configFile.writeText(json.encodeToString(config))
    }

    // Merges the loaded JSON with the default values
    private inline fun <reified T> loadMerged(loadedConfig: String?, defaultConfig: T): T {
        if (loadedConfig == null) return defaultConfig
        val loaded = json.parseToJsonElement(loadedConfig).jsonObject
        val default = json.encodeToJsonElement(defaultConfig).jsonObject
        return json.decodeFromJsonElement(JsonObject(default + loaded))
    }

    // Loads the language file from the mod resources
    private fun loadLang(logger: Logger): LangConfig {
        val path = "/assets/$MOD_ID/lang/${config.language}.json"
        val stream = javaClass.getResourceAsStream(path)

        // Placeholders
        val values = mapOf("language" to config.language)

        // Fallback to defaults if the language file is missing
        if (stream == null) {
            logger.warn(Placeholder.replace(lang.logLangMissing, values))
            logger.warn(lang.logLangMissingFallback)
            return LangConfig()
        }

        return loadMerged(stream.reader().readText(), LangConfig())
    }
}