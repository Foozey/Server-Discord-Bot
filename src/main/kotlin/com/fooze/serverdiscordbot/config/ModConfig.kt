package com.fooze.serverdiscordbot.config

import kotlinx.serialization.Serializable

@Serializable
data class ModConfig(
    // Language
    val language: String = "en_us",

    // Discord
    val discordBotToken: String = "",
    val discordChannelId: String = "",

    // Server
    val serverName: String = "",
    val serverIp: String = "",
    val serverModpackName: String = "",
    val serverModpackVersion: String = "",
    val serverModpackUrl: String = "",

    // Milestones
    val milestoneDeaths: Int = 10,
    val milestonePlayerKills: Int = 10,
    val milestoneMobKills: Int = 1000,
    val milestoneBlocksMined: Int = 10000,
    val milestoneBlocksPlaced: Int = 10000,
    val milestoneItemsCrafted: Int = 10000,
    val milestoneTimePlayed: Int = 100
)