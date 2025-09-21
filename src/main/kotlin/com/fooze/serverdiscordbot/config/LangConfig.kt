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
    val announcePresence: String = "{count} player online",
    val announcePresencePlural: String = "{count} players online",

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
    val whitelistAddTitle: String = "Added {player} to the whitelist",
    val whitelistAddDescription: String = "Welcome to {server}!",
    val whitelistAddDescriptionInfo: String = "To join the server, make sure you're playing **{type}**, then connect using the IP below:",
    val whitelistIpMissing: String = "Unavailable",

    // Stats Command
    val statsCommand: String = "stats",
    val statsCommandInfo: String = "Displays a player's statistics",
    val statsCommandPlayer: String = "player",
    val statsCommandPlayerInfo: String = "The player to display statistics for",
    val statsInvalid: String = "**{player}** not found! The name may be invalid",
    val statsTitle: String = "{player}'s Stats",
    val statsDescription: String = "Displaying statistics for {player} on {server}",
    val statsUpdate: String = "Last updated <t:{time}:R>, use </{stats}:{id}> to update",
    val statsGeneral: String = "General",
    val statsDeaths: String = "Deaths",
    val statsPlayerKills: String = "Player Kills",
    val statsMobKills: String = "Mob Kills",
    val statsBlocksMined: String = "Blocks Mined",
    val statsBlocksPlaced: String = "Blocks Placed",
    val statsItemsCrafted: String = "Items Crafted",
    val statsTimePlayed: String = "Time Played",

    // Help Command
    val helpCommand: String = "help",
    val helpCommandInfo: String = "Displays a list of commands",
    val helpTitle: String = "Command Help",
    val helpDescription: String = "Displaying a list of commands for {server}",
    val helpHelp: String = "</{help}:{helpId}> — Displays a list of commands",
    val helpStatus: String = "</{status}:{statusId}> — Displays the server status",
    val helpWhitelist: String = "</{whitelist}:{whitelistId}> — Adds a player to the whitelist",
    val helpWhitelistUsage: String = "Usage: /{whitelist} <player>",
    val helpStats: String = "</{stats}:{statsId}> — Displays a player's statistics",
    val helpStatsUsage: String = "Usage: /{stats} <player>",

    // Milestones
    val milestoneDeaths: String = "{player} has died {count} times!",
    val milestonePlayerKills: String = "{player} has killed {count} players!",
    val milestoneMobKills: String = "{player} has killed {count} mobs!",
    val milestoneBlocksMined: String = "{player} has mined {count} blocks!",
    val milestoneBlocksPlaced: String = "{player} has placed {count} blocks!",
    val milestoneItemsCrafted: String = "{player} has crafted {count} items!",
    val milestoneTimePlayed: String = "{player} has played for {count} hours!",

    // Other
    val defaultServerName: String = "The server",
    val defaultServerType: String = "Minecraft {version}",

    // Logging
    val logBotTokenMissing: String = "Bot token missing! Add it to config/${MOD_ID}.json",
    val logChannelIdMissing: String = "Channel ID missing! Add it to config/${MOD_ID}.json",
    val logLangMissing: String = "Unable to find language file for: {language}",
    val logLangMissingFallback: String = "Falling back to default language (en_us)",
    val logLoginSuccess: String = "Login successful!",
    val logLoginFail: String = "Login failed! Your bot token may be invalid",
    val logAnnounceFail: String = "Announcement failed! Your channel ID may be invalid",
    val logCommandFail: String = "{command} command failed to initialize! Your channel ID may be invalid",
)