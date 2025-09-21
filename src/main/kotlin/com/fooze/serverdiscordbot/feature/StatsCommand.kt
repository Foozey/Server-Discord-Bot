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
                    val world = server.saveProperties.levelName
                    val file = server.runDirectory.resolve("${world}/stats/${profile.id}.json").toFile()
                    val stats = ServerStatHandler(server, file)

                    title = Placeholder.replace(lang.statsTitle, values)
                    description = Placeholder.replace(lang.statsDescription, values)
                    thumbnail { url = "https://mc-heads.net/player/${name}" }

                    // Stat fields
                    field("")
                    field(lang.statsDeaths, true) { "```${getStat(stats, Stats.DEATHS)}```" }
                    field(lang.statsPlayerKills, true) { "```${getStat(stats, Stats.PLAYER_KILLS)}```" }
                    field(lang.statsMobKills, true) { "```${getStat(stats, Stats.MOB_KILLS)}```" }
                    field(lang.statsBlocksMined, true) { "```${getTotal(stats, Registries.BLOCK, Stats.MINED) { it }}```" }
                    field(lang.statsBlocksPlaced, true) { "```${getTotal(stats, Registries.BLOCK, Stats.USED) { it.asItem() }}```" }
                    field(lang.statsItemsCrafted, true) { "```${getTotal(stats, Registries.ITEM, Stats.CRAFTED) { it }}```" }
                    field(lang.statsTimePlayed, true) { "```${formatHours(getStat(stats, Stats.PLAY_TIME))}```" }
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

    // Returns the time in hours for the provided ticks
    private fun formatHours(ticks: Int): String {
        val hours = ticks.toDouble() / 72000
        return String.format("%.1f", hours).removeSuffix(".0") + " hours"
    }
}