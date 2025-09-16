package com.fooze.serverdiscordbot.config

import kotlinx.serialization.Serializable

@Serializable
data class ModConfig(
    // Discord
    val botToken: String = "",
    val channelId: String = "",

    // Server
    val serverName: String = "",
    val serverIp: String = "",
    val modpackName: String = "",
    val modpackVersion: String = "",
    val modpackUrl: String = ""
)