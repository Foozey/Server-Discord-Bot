package com.fooze.serverdiscordbot.config

import kotlinx.serialization.Serializable

@Serializable
data class LangConfig(
    // Announcements
    val announceStartTitle: String = "Server started",
    val announceStartDescription: String = "{server} is now online — connect using `{ip}`",
    val announceStopTitle: String = "Server stopped",
    val announceStopDescription: String = "{server} is now offline — see you soon!",
    val announceJoin: String = "{player} joined the game",
    val announceJoinDescription: String = "\uD83D\uDD25 Daily streak: {streak}",
    val announceLeave: String = "{player} left the game",
    val announceDeathDescription: String = "Total deaths: {deaths}",
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

    // Leaderboard Command
    val leaderboardCommand: String = "leaderboard",
    val leaderboardCommandInfo: String = "Displays a stat's leaderboard",
    val leaderboardCommandStat: String = "stat",
    val leaderboardCommandStatInfo: String = "The stat to display the leaderboard for",
    val leaderboardTitle: String = "{statTitle} Leaderboard",
    val leaderboardDescription: String = "Displaying the top 10 players for {statDescription} on {server}",
    val leaderboardEmpty: String = "No player stats found! Your server may not have any players yet",
    val leaderboardUpdate: String = "Last updated <t:{time}:R>, use </{leaderboard}:{id}> to update",

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
    val helpLeaderboard: String = "</{leaderboard}:{leaderboardId}> — Displays a stat's leaderboard",
    val helpLeaderboardUsage: String = "Usage: /{leaderboard} <stat>",

    // Milestones
    val milestoneDeaths: String = "{player} has died {milestone} times!",
    val milestonePlayerKills: String = "{player} has killed {milestone} players!",
    val milestoneMobKills: String = "{player} has killed {milestone} mobs!",
    val milestoneBlocksMined: String = "{player} has mined {milestone} blocks!",
    val milestoneBlocksPlaced: String = "{player} has placed {milestone} blocks!",
    val milestoneItemsCrafted: String = "{player} has crafted {milestone} items!",
    val milestoneTimePlayed: String = "{player} has played for {milestone} hours!",

    // Other
    val defaultServerName: String = "The server",
    val defaultServerType: String = "Minecraft {version}",

    // Logging
    val logBotTokenMissing: String = "Bot token missing! Add it to config/{modid}/config.json",
    val logChannelIdMissing: String = "Channel ID missing! Add it to config/{modid}/config.json",
    val logLangMissing: String = "Unable to find language file for: {language}",
    val logLangMissingFallback: String = "Falling back to default language (en_us)",
    val logLoginSuccess: String = "Login successful!",
    val logLoginFail: String = "Login failed! Your bot token may be invalid",
    val logAnnounceFail: String = "Announcement failed! Your channel ID may be invalid",
    val logCommandFail: String = "{command} command failed to initialize! Your channel ID may be invalid",
)