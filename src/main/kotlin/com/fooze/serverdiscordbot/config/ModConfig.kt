package com.fooze.serverdiscordbot.config

import kotlinx.serialization.Serializable

@Serializable
data class ModConfig(
    val botToken: String = "",
    val channelId: String = ""
)