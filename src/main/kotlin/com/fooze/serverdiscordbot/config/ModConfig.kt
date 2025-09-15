package com.fooze.serverdiscordbot.config

import kotlinx.serialization.Serializable

@Serializable
data class ModConfig(
    val botToken: String = "",
    val channelId: String = "",
    val serverIp: String = "",
    val modpackName: String = "",
    val modpackVersion: String = "",
    val modpackUrl: String = ""
)