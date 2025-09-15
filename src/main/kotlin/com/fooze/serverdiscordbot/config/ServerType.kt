package com.fooze.serverdiscordbot.config

import net.minecraft.server.MinecraftServer

object ServerType {
    fun getServerType(config: ModConfig, server: MinecraftServer): String {
        val hasName = config.modpackName.isNotBlank()
        val hasVersion = config.modpackVersion.isNotBlank()
        val hasUrl = config.modpackUrl.isNotBlank()

        val serverType = when {
            hasName && hasVersion -> "${config.modpackName} ${config.modpackVersion}"
            hasName -> config.modpackName
            else -> "Minecraft ${server.version}"
        }

        return when {
            hasUrl && hasName -> "[$serverType](${config.modpackUrl})"
            hasUrl -> config.modpackUrl
            else -> serverType
        }
    }
}