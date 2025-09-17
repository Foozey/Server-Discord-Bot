package com.fooze.serverdiscordbot.util

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import net.minecraft.server.MinecraftServer

object Format {
    // Returns the server's name or the default if unavailable
    fun serverName(config: ModConfig, lang: LangConfig, capitalize: Boolean): String {
        return config.serverName.ifBlank {
            if (capitalize) lang.defaultServerName else lang.defaultServerName.lowercase()
        }
    }

    // Returns the server's type based on the configured modpack info
    fun serverType(config: ModConfig, lang: LangConfig, server: MinecraftServer): String {
        val hasName = config.serverModpackName.isNotBlank()
        val hasVersion = config.serverModpackVersion.isNotBlank()
        val hasUrl = config.serverModpackUrl.isNotBlank()
        val values = mapOf("version" to server.version)

        val serverType = when {
            hasName && hasVersion -> "${config.serverModpackName} ${config.serverModpackVersion}"
            hasName -> config.serverModpackName
            else -> Placeholder.replace(lang.defaultServerType, values)
        }

        return when {
            hasUrl && hasName -> "[$serverType](${config.serverModpackUrl})"
            hasUrl -> config.serverModpackUrl
            else -> serverType
        }
    }
}