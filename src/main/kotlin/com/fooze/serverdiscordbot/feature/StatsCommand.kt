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
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.stat.ServerStatHandler
import net.minecraft.stat.StatType
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
                    val player = server.playerManager.getPlayer(name)

                    // If the player is online, use their server stats, otherwise use the stat file
                    val stats = if (player != null) {
                        player.statHandler
                    } else {
                        val world = server.saveProperties.levelName
                        val file = server.runDirectory.resolve("${world}/stats/${profile.id}.json").toFile()
                        ServerStatHandler(server, file)
                    }

                    // Stat values
                    val deaths = String.format("%,d", getStat(stats, Stats.DEATHS))
                    val playerKills = String.format("%,d", getStat(stats, Stats.PLAYER_KILLS))
                    val mobKills = String.format("%,d", getStat(stats, Stats.MOB_KILLS))
                    val blocksMined = String.format("%,d", getTotal(stats, Registries.BLOCK, Stats.MINED) { it })
                    val blocksPlaced = String.format("%,d", getTotal(stats, Registries.BLOCK, Stats.USED) { it.asItem() })
                    val itemsCrafted = String.format("%,d", getTotal(stats, Registries.ITEM, Stats.CRAFTED) { it })
                    val timePlayed = String.format("%,.1f", getStat(stats, Stats.PLAY_TIME) / 72000.0).removeSuffix(".0") + " hours"

                    title = Placeholder.replace(lang.statsTitle, values)
                    description = Placeholder.replace(lang.statsDescription, values)
                    thumbnail { url = "https://mc-heads.net/player/${name}" }

                    // Stat fields
                    field("")
                    field(lang.statsDeaths, true) { "```$deaths```" }
                    field(lang.statsPlayerKills, true) { "```$playerKills```" }
                    field(lang.statsMobKills, true) { "```$mobKills```" }
                    field(lang.statsBlocksMined, true) { "```$blocksMined```" }
                    field(lang.statsBlocksPlaced, true) { "```$blocksPlaced```" }
                    field(lang.statsItemsCrafted, true) { "```$itemsCrafted```" }
                    field(lang.statsTimePlayed, true) { "```$timePlayed```" }
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

    // Returns the value of the provided stat
    fun getStat(stats: ServerStatHandler, stat: Identifier): Int {
        return stats.getStat(Stats.CUSTOM.getOrCreateStat(stat))
    }

    // Returns the total of the provided stats
    fun <T, R> getTotal(
        stats: ServerStatHandler,
        registry: DefaultedRegistry<T>,
        type: StatType<R>,
        map: (T) -> R
    ): Int {
        return registry.sumOf { entry ->
            map(entry)?.let { stats.getStat(type.getOrCreateStat(it)) } ?: 0
        }
    }
}