package com.fooze.serverdiscordbot.config

import kotlinx.serialization.Serializable

@Serializable
data class ModConfig(
    // Discord
    val discordBotToken: String = "",
    val discordChannelId: String = "",

    // Server
    val serverName: String = "",
    val serverIp: String = "",
    val serverModpackName: String = "",
    val serverModpackVersion: String = "",
    val serverModpackUrl: String = ""
)