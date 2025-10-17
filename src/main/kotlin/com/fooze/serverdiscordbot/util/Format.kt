package com.fooze.serverdiscordbot.util

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import net.minecraft.server.MinecraftServer

object Format {
    // Returns the server's name or the default if unavailable
    fun serverName(config: ModConfig, lang: LangConfig, capitalize: Boolean): String {
        return if (config.serverName.isNotBlank()) {
            escape(config.serverName)
        } else {
            if (capitalize) {
                lang.defaultServerName
            } else {
                lang.defaultServerName.lowercase()
            }
        }
    }

    // Returns the server's type based on the configured modpack info
    fun serverType(server: MinecraftServer, config: ModConfig, lang: LangConfig): String {
        val hasName = config.serverModpackName.isNotBlank()
        val hasVersion = config.serverModpackVersion.isNotBlank()
        val hasUrl = config.serverModpackUrl.isNotBlank()

        // Placeholders
        val placeholders = mapOf("version" to server.version)

        val serverType = when {
            hasName && hasVersion -> escape("${config.serverModpackName} ${config.serverModpackVersion}")
            hasName -> escape(config.serverModpackName)
            else -> replace(lang.defaultServerType, placeholders)
        }

        return when {
            hasUrl && hasName -> "[${serverType}](${config.serverModpackUrl})"
            hasUrl -> config.serverModpackUrl
            else -> serverType
        }
    }

    // Formats a number with thousands separators
    fun number(value: Any) = String.format("%,d", value)

    // Formats a number with thousands separators to 1 decimal place
    fun decimal(value: Any) = String.format("%,.1f", value).removeSuffix(".0")

    // Converts ticks to hours and formats the number with thousands separators to 1 decimal place
    fun hours(value: Int) = String.format("%,.1f", value / 72000.0).removeSuffix(".0") + " hours"

    // Replaces placeholders using values from a map
    fun replace(template: String, values: Map<String, String>): String {
        var result = template

        for ((key, value) in values) {
            result = result.replace("{${key}}", value)
        }

        return result
    }

    // Escapes markdown characters in a string
    fun escape(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("*", "\\*")
            .replace("_", "\\_")
            .replace("~", "\\~")
            .replace("`", "\\`")
            .replace("#", "\\#")
            .replace("-", "\\-")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace(">", "\\>")
    }
}