package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Colors
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
import net.minecraft.util.Identifier
import java.time.Instant

// TODO: Paginated stats by category
object StatsCommand : Command ({ it.statsCommand }, { it.statsCommandInfo }) {
    override suspend fun run(
        event: GuildChatInputCommandInteractionCreateEvent,
        config: ModConfig,
        lang: LangConfig,
        server: MinecraftServer?
    ) {
        if (server == null) return

        val name = event.interaction.command.strings[lang.statsCommandPlayer]
        val profile = server.gameProfileRepo.findProfileByName(name).orElse(null)

        // Placeholders
        val values = mapOf(
            "player" to name.toString(),
            "server" to Format.serverName(config, lang, false),
            "time" to Instant.now().epochSecond.toString(),
            "stats" to lang.statsCommand,
            "id" to command?.id.toString()
        )

        // Build the embed
        event.interaction.deferPublicResponse().respond {
            embed {
                if (profile == null) {
                    description = Placeholder.replace(lang.statsInvalid, values)
                    color = Colors.RED
                } else {
                    val world = server.saveProperties.levelName
                    val file = server.runDirectory.resolve("${world}/stats/${profile.id}.json").toFile()
                    val stats = ServerStatHandler(server, file)

                    title = Placeholder.replace(lang.statsTitle, values)
                    description = Placeholder.replace(lang.statsDescription, values)
                    thumbnail { url = "https://mc-heads.net/player/${name}" }
                    author { this.name = "\uD83C\uDF10 ${lang.statsGeneral}" }

                    // Player stats
                    field("")
                    field(lang.statsDeaths, true) { "```${getStat(stats, Stats.DEATHS)}```" }
                    field(lang.statsPlayerKills, true) { "```${getStat(stats, Stats.PLAYER_KILLS)}```" }
                    field(lang.statsMobKills, true) { "```${getStat(stats, Stats.MOB_KILLS)}```" }
                    field(lang.statsBlocksMined, true) { "```${getBlocksMined(stats)}```" }
                    field(lang.statsBlocksPlaced, true) { "```${getBlocksPlaced(stats)}```" }
                    field(lang.statsItemsCrafted, true) { "```${getItemsCrafted(stats)}```" }
                    field(lang.statsTimePlayed, true) { "```${formatTime(getStat(stats, Stats.PLAY_TIME))}```" }
                    field("")

                    // Last updated footer
                    field("") { "-# ${Placeholder.replace(lang.statsUpdate, values)}\n" }
                }
            }
        }
    }

    // Defines the player as a required command option
    override suspend fun options(builder: ChatInputCreateBuilder, lang: LangConfig) {
        builder.string(lang.statsCommandPlayer, lang.statsCommandPlayerInfo) {
            required = true
        }
    }

    // Returns the value of the provided stat for the player
    private fun getStat(stats: ServerStatHandler, stat: Identifier): Int {
        return stats.getStat(Stats.CUSTOM.getOrCreateStat(stat))
    }

    // Returns the total blocks mined by the player
    private fun getBlocksMined(stats: ServerStatHandler,): Int {
        return Registries.BLOCK.sumOf { stats.getStat(Stats.MINED.getOrCreateStat(it)) }
    }

    // Returns the total blocks placed by the player
    private fun getBlocksPlaced(stats: ServerStatHandler,): Int {
        return Registries.BLOCK.sumOf { block ->
            block.asItem()?.let { stats.getStat(Stats.USED.getOrCreateStat(it)) } ?: 0
        }
    }

    // Returns the total items crafted by the player
    private fun getItemsCrafted(stats: ServerStatHandler): Int {
        return Registries.ITEM.sumOf { stats.getStat(Stats.CRAFTED.getOrCreateStat(it)) }
    }

    // Returns the time in hours for the provided ticks
    private fun formatTime(ticks: Int): String {
        val hours = ticks.toDouble() / 72000

        return if (hours % 1.0 == 0.0) {
            String.format("%d hours", hours.toInt())
        } else {
            String.format("%.1f hours", hours)
        }
    }
}