package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Format
import com.fooze.serverdiscordbot.util.Placeholder
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.stat.ServerStatHandler
import net.minecraft.stat.Stats
import java.time.Instant
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

object LeaderboardCommand : Command ({ it.leaderboardCommand }, { it.leaderboardCommandInfo }) {
    override suspend fun run(
        event: GuildChatInputCommandInteractionCreateEvent,
        config: ModConfig,
        lang: LangConfig,
        server: MinecraftServer?
    ) {
        if (server == null) return

        val response = event.interaction.deferPublicResponse()
        val playerStats = mutableListOf<Pair<String, Int>>()
        val stat = event.interaction.command.strings[lang.leaderboardCommandStat]
        val statNames = statNames(lang)
        val folder = server.runDirectory.resolve("${server.saveProperties.levelName}/stats")
        if (!folder.exists()) return

        // Get each player from the stats folder and their stat values
        folder.listDirectoryEntries().forEach { file ->
            val uuid = UUID.fromString(file.nameWithoutExtension)
            val name = server.apiServices.profileResolver.getProfileById(uuid).get().name
            val player = server.playerManager.getPlayer(name)

            // If the player is online, use their server stats, otherwise use the stat file
            val stats = if (player != null) {
                player.statHandler
            } else {
                ServerStatHandler(server, file.toFile())
            }

            // Get the stat value of the input stat
            val value = when (stat) {
                "deaths" -> StatsCommand.getStat(stats, Stats.DEATHS)
                "playerKills" -> StatsCommand.getStat(stats, Stats.PLAYER_KILLS)
                "mobKills" -> StatsCommand.getStat(stats, Stats.MOB_KILLS)
                "blocksMined" -> StatsCommand.getTotal(stats, Registries.BLOCK, Stats.MINED) { it }
                "blocksPlaced" -> StatsCommand.getTotal(stats, Registries.BLOCK, Stats.USED) { it.asItem() }
                "itemsCrafted" -> StatsCommand.getTotal(stats, Registries.ITEM, Stats.CRAFTED) { it }
                "timePlayed" -> StatsCommand.getStat(stats, Stats.PLAY_TIME)
                else -> 0
            }

            playerStats.add(name to value)
        }

        // Sort the top 10 players by descending stat values
        val topPlayers = playerStats.sortedByDescending { it.second }.take(10)

        // Placeholders
        val values = mapOf(
            "stat" to statNames[stat].toString(),
            "count" to String.format("%,d", topPlayers.size),
            "server" to Format.serverName(config, lang, false),
            "time" to Instant.now().epochSecond.toString(),
            "leaderboard" to lang.leaderboardCommand,
            "id" to command?.id.toString()
        )

        // Build the embed
        response.respond {
            embed {
                title = Placeholder.replace(lang.leaderboardTitle, values)
                description = Placeholder.replace(lang.leaderboardDescription, values)
                field("")

                // Top players list
                field("") {
                    topPlayers.mapIndexed { index, (name, value) ->
                        // Format the stat value based on the stat type
                        val formattedValue = if (stat == "timePlayed") {
                            String.format("%,.1f", value / 72000.0).removeSuffix(".0") + " hours"
                        } else {
                            String.format("%,d", value)
                        }

                        // Give the top 3 players medals
                        val medal = when (index) {
                            0 -> "\uD83E\uDD47"
                            1 -> "\uD83E\uDD48"
                            2 -> "\uD83E\uDD49"
                            else -> ""
                        }

                        "**#${index + 1}** ${medal} ${Format.escapeMarkdown(name)}: `${formattedValue}`"
                    }.joinToString("\n")
                }

                // Last updated footer
                field("")
                field("") { "-# ${Placeholder.replace(lang.leaderboardUpdate, values)}\n" }
            }
        }
    }

    // Defines the stat as a required command option with choices
    override suspend fun options(builder: ChatInputCreateBuilder, lang: LangConfig) {
        val statNames = statNames(lang)

        builder.string(lang.leaderboardCommandStat, lang.leaderboardCommandStatInfo) {
            required = true
            statNames.forEach { (value, name) -> choice(name, value) }
        }
    }

    // Maps stats to stat names
    private fun statNames(lang: LangConfig) = mapOf(
        "deaths" to lang.statsDeaths,
        "playerKills" to lang.statsPlayerKills,
        "mobKills" to lang.statsMobKills,
        "blocksMined" to lang.statsBlocksMined,
        "blocksPlaced" to lang.statsBlocksPlaced,
        "itemsCrafted" to lang.statsItemsCrafted,
        "timePlayed" to lang.statsTimePlayed,
    )
}