package com.fooze.serverdiscordbot.feature.commands

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Colors
import com.fooze.serverdiscordbot.util.Format
import com.fooze.serverdiscordbot.util.PlayerStats
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import net.minecraft.server.MinecraftServer
import java.time.Instant
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

object LeaderboardCommand : Command({ it.leaderboardCommand }, { it.leaderboardCommandInfo }) {
    override suspend fun run(
        server: MinecraftServer?,
        config: ModConfig,
        lang: LangConfig,
        event: GuildChatInputCommandInteractionCreateEvent
    ) {
        if (server == null) return

        val response = event.interaction.deferPublicResponse()

        // Get the stat key and its name and value
        val key = event.interaction.command.strings[lang.leaderboardCommandStat]
        val (statName, statValue) = stats(lang)[key]!!

        // Set of players who have stats available
        val players = mutableSetOf<UUID>()

        // If the stats folder exists, add all player UUIDs to the set
        val world = server.saveProperties.levelName
        val folder = server.runDirectory.resolve("${world}/stats")

        if (folder.exists()) {
            for (file in folder.listDirectoryEntries()) {
                runCatching {
                    players.add(UUID.fromString(file.nameWithoutExtension))
                }
            }
        }

        // Add all online player UUIDs to the set
        for (player in server.playerManager.playerList) {
            players.add(player.uuid)
        }

        // If the player set is empty, send a warning message
        if (players.isEmpty()) {
            response.respond {
                embed {
                    description = lang.leaderboardEmpty
                    color = Colors.YELLOW
                }
            }

            return
        }

        // Get player stats using the UUIDs in the player set
        val playerStats = players.mapNotNull { uuid ->
            runCatching {
                val profile = server.apiServices.profileResolver.getProfileById(uuid).orElse(null)
                profile.name to statValue(PlayerStats.get(server, profile))
            }.getOrNull()
        }

        // Placeholders
        val placeholders = mapOf(
            "statTitle" to statName,
            "statDescription" to statName.lowercase(),
            "server" to Format.serverName(config, lang, false),
            "time" to Instant.now().epochSecond.toString(),
            "leaderboard" to lang.leaderboardCommand,
            "id" to command?.id.toString()
        )

        // Build the embed
        response.respond {
            embed {
                title = Format.replace(lang.leaderboardTitle, placeholders)
                description = Format.replace(lang.leaderboardDescription, placeholders)

                // Leaderboard
                field("")
                field("") { getLeaderboard(playerStats, key) }
                field("")

                // Last updated footer
                field("") { "-# ${Format.replace(lang.leaderboardUpdate, placeholders)}\n" }
            }
        }
    }

    // Defines the stat as a required command option with choices
    override suspend fun options(lang: LangConfig, builder: ChatInputCreateBuilder) {
        builder.string(lang.leaderboardCommandStat, lang.leaderboardCommandStatInfo) {
            required = true

            for ((key, stat) in stats(lang)) {
                choice(stat.first, key)
            }
        }
    }

    // Returns a map of stat keys to their names and values
    private fun stats(lang: LangConfig): Map<String, Pair<String, (PlayerStats.PlayerStats) -> Int>> = mapOf(
        "deaths" to (lang.statsDeaths to { it.deaths }),
        "playerKills" to (lang.statsPlayerKills to { it.playerKills }),
        "mobKills" to (lang.statsMobKills to { it.mobKills }),
        "blocksMined" to (lang.statsBlocksMined to { it.blocksMined }),
        "blocksPlaced" to (lang.statsBlocksPlaced to { it.blocksPlaced }),
        "itemsCrafted" to (lang.statsItemsCrafted to { it.itemsCrafted }),
        "timePlayed" to (lang.statsTimePlayed to { it.timePlayed })
    )

    // Returns the top 10 players of a stat as a formatted list
    private fun getLeaderboard(playerStats: List<Pair<String, Int>>, key: String?): String {
        val list = playerStats.sortedByDescending { it.second }.take(10)

        return list.mapIndexed { index, (name, value) ->
            // Format the value based on the stat key
            val formattedValue = if (key == "timePlayed") {
                Format.hours(value)
            } else {
                Format.number(value)
            }

            // Add medals based on the index
            val medal = when (index) {
                0 -> "\uD83E\uDD47"
                1 -> "\uD83E\uDD48"
                2 -> "\uD83E\uDD49"
                else -> ""
            }

            "**#${index + 1}** ${medal} ${Format.escape(name)}: `${formattedValue}`"
        }.joinToString("\n")
    }
}