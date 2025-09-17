package com.fooze.serverdiscordbot.config

import com.fooze.serverdiscordbot.ServerDiscordBot.MOD_ID
import kotlinx.serialization.Serializable

@Serializable
data class LangConfig(
    // Announcements
    val announceStartTitle: String = "Server started",
    val announceStartDescription: String = "{server} is now online — connect using `{ip}`",
    val announceStopTitle: String = "Server stopped",
    val announceStopDescription: String = "{server} is now offline — see you soon!",
    val announceJoin: String = "{player} joined the game",
    val announceLeave: String = "{player} left the game",
    val announceDeath: String = "{player} died",
    val announceDeathTotal: String = "Total deaths: {deaths}",

    // Status Command
    val statusCommand: String = "status",
    val statusCommandInfo: String = "Displays the server status",
    val statusTitle: String = "Server Status",
    val statusDescription: String = "Displaying server status for {server}",
    val statusState: String = "State",
    val statusStateValue: String = "Online",
    val statusTps: String = "TPS",
    val statusMspt: String = "MSPT",
    val statusCpu: String = "CPU Usage",
    val statusRam: String = "RAM Usage",
    val statusPlayers: String = "Players ({count})",
    val statusPlayersNone: String = "No players online",
    val statusPlayersMore: String = "...and {remaining} more",
    val statusUpdate: String = "Last updated <t:{time}:R>, use </{status}:{id}> to update",

    // Whitelist Command
    val whitelistCommand: String = "whitelist",
    val whitelistCommandInfo: String = "Adds a player to the whitelist",
    val whitelistCommandPlayer: String = "player",
    val whitelistCommandPlayerInfo: String = "The player to whitelist",
    val whitelistInvalid: String = "**{player}** not found! The name may be invalid",
    val whitelistExisting: String = "**{player}** is already whitelisted",
    val whitelistAdd: String = "Added {player} to the whitelist",
    val whitelistAddTitle: String = "Welcome to {server}!",
    val whitelistAddDescription: String = "To join, make sure you're playing **{type}**, and connect using the IP below:",
    val whitelistIpMissing: String = "Unavailable",

    // Help Command
    val helpCommand: String = "help",
    val helpCommandInfo: String = "Displays a list of commands",
    val helpTitle: String = "Command Help",
    val helpDescription: String = "Displaying a list of commands for {server}",
    val helpStatusTitle: String = "</{status}:{statusId}>",
    val helpStatusDescription: String = "Displays the server status",
    val helpWhitelistTitle: String = "</{whitelist}:{whitelistId}>",
    val helpWhitelistDescription: String = "Adds a player to the whitelist\n-# Usage: /{whitelist} <player>",

    // Other
    val defaultServerName: String = "The server",
    val defaultServerType: String = "Minecraft {version}",

    // Logging
    val logBotTokenMissing: String = "Bot token missing! Add it to config/${MOD_ID}/config.json",
    val logChannelIdMissing: String = "Channel ID missing! Add it to config/${MOD_ID}/config.json",
    val logLoginSuccess: String = "Login successful!",
    val logLoginFail: String = "Login failed! Your bot token may be invalid",
    val logAnnounceFail: String = "Announcement failed! Your channel ID may be invalid",
    val logStatusFail: String = "Status command failed to initialize! Your channel ID may be invalid",
    val logWhitelistFail: String = "Whitelist command failed to initialize! Your channel id may be invalid"
)