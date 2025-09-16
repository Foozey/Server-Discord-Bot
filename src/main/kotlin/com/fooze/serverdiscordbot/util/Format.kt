package com.fooze.serverdiscordbot.util

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import net.minecraft.server.MinecraftServer

object Format {
    fun serverName(config: ModConfig, lang: LangConfig, capitalize: Boolean): String {
        return config.serverName.ifBlank {
            if (capitalize) lang.defaultServerName else lang.defaultServerName.lowercase()
        }
    }

    fun serverType(config: ModConfig, lang: LangConfig, server: MinecraftServer): String {
        val hasName = config.modpackName.isNotBlank()
        val hasVersion = config.modpackVersion.isNotBlank()
        val hasUrl = config.modpackUrl.isNotBlank()
        val values = mapOf("version" to server.version)

        // Use modpack info when available
        val serverType = when {
            hasName && hasVersion -> "${config.modpackName} ${config.modpackVersion}"
            hasName -> config.modpackName
            else -> Placeholder.replace(lang.defaultServerType, values)
        }

        // Use modpack url when available
        return when {
            hasUrl && hasName -> "[$serverType](${config.modpackUrl})"
            hasUrl -> config.modpackUrl
            else -> serverType
        }
    }
}