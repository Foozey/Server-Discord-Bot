package com.fooze.serverdiscordbot.config

import com.fooze.serverdiscordbot.ServerDiscordBot.MOD_ID
import com.fooze.serverdiscordbot.util.Format
import kotlinx.serialization.json.*
import org.slf4j.Logger
import java.io.File

object ConfigHandler {
    var config: ModConfig = ModConfig()
    var lang: LangConfig = LangConfig()
    private val file = File("config/${MOD_ID}/config.json")
    private var isValid = true

    // JSON serializer
    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // Loads the config from the file if it exists, otherwise uses the default values
    fun load(logger: Logger) {
        val loadedConfig = if (file.exists()) {
            file.readText()
        } else {
            null
        }

        config = loadMerged(logger, loadedConfig, ModConfig(), file.path)
        lang = loadLang(logger)
        save(logger, lang)
    }

    // Saves the config to the file if the config is valid
    private fun save(logger: Logger, lang: LangConfig) {
        if (!isValid) return

        runCatching {
            file.parentFile.mkdirs()
            file.writeText(json.encodeToString(config))
        }.onFailure {
            // Placeholders
            val placeholders = mapOf("path" to file.path)

            logger.error(lang.logConfigSaveFail, placeholders)
            logger.error(it.message)
        }
    }

    // Merges the loaded JSON with the default values
    private inline fun <reified Config> loadMerged(
        logger: Logger,
        loadedConfig: String?,
        defaultConfig: Config,
        path: String
    ): Config {
        // If the config file is missing, fallback to defaults
        if (loadedConfig == null) {
            return defaultConfig
        }

        return runCatching {
            val loaded = json.parseToJsonElement(loadedConfig).jsonObject
            val default = json.encodeToJsonElement(defaultConfig).jsonObject
            json.decodeFromJsonElement<Config>(JsonObject(default + loaded))
        }.getOrElse {
            // Placeholders
            val placeholders = mapOf("path" to path)

            // If the config file is invalid, fallback to defaults
            logger.error(Format.replace(lang.logConfigInvalid, placeholders))
            logger.error(it.message)
            isValid = false
            defaultConfig
        }
    }

    // Loads the language file from the mod resources
    private fun loadLang(logger: Logger): LangConfig {
        val path = "/assets/${MOD_ID}/lang/${config.language}.json"
        val stream = javaClass.getResourceAsStream(path)

        // Placeholders
        val placeholders = mapOf("path" to path)

        // If the language file is missing, fallback to defaults
        if (stream == null) {
            logger.warn(Format.replace(lang.logLangMissing, placeholders))
            return LangConfig()
        }

        return loadMerged(logger, stream.reader().readText(), LangConfig(), path)
    }
}