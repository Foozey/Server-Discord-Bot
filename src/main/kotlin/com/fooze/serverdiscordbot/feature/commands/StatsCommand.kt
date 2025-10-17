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

object StatsCommand : Command({ it.statsCommand }, { it.statsCommandInfo }) {
    override suspend fun run(
        server: MinecraftServer?,
        config: ModConfig,
        lang: LangConfig,
        event: GuildChatInputCommandInteractionCreateEvent
    ) {
        if (server == null) return

        val response = event.interaction.deferPublicResponse()
        val name = event.interaction.command.strings[lang.statsCommandPlayer]
        val profile = server.apiServices.profileResolver.getProfileByName(name).orElse(null)

        // Placeholders
        val placeholders = mapOf(
            "player" to Format.escape(name.toString()),
            "server" to Format.serverName(config, lang, false),
            "time" to Instant.now().epochSecond.toString(),
            "stats" to lang.statsCommand,
            "id" to command?.id.toString()
        )

        // Build the embed
        response.respond {
            embed {
                // If the profile is invalid, send an error message
                if (profile == null) {
                    description = Format.replace(lang.statsInvalid, placeholders)
                    color = Colors.RED
                }

                // Display player stats for the given player
                else {
                    val stats = PlayerStats.get(server, profile)

                    title = Format.replace(lang.statsTitle, placeholders)
                    description = Format.replace(lang.statsDescription, placeholders)
                    thumbnail { url = "https://mc-heads.net/player/${name}" }

                    // Stat fields
                    field("")
                    field(lang.statsDeaths, true) { "```${Format.number(stats.deaths)}```" }
                    field(lang.statsPlayerKills, true) { "```${Format.number(stats.playerKills)}```" }
                    field(lang.statsMobKills, true) { "```${Format.number(stats.mobKills)}```" }
                    field(lang.statsBlocksMined, true) { "```${Format.number(stats.blocksMined)}```" }
                    field(lang.statsBlocksPlaced, true) { "```${Format.number(stats.blocksPlaced)}```" }
                    field(lang.statsItemsCrafted, true) { "```${Format.number(stats.itemsCrafted)}```" }
                    field(lang.statsTimePlayed, true) { "```${Format.hours(stats.timePlayed)}```" }
                    field("")

                    // Last updated footer
                    field("") { "-# ${Format.replace(lang.statsUpdate, placeholders)}\n" }
                }
            }
        }
    }

    // Defines the player as a required command option
    override suspend fun options(lang: LangConfig, builder: ChatInputCreateBuilder) {
        builder.string(lang.statsCommandPlayer, lang.statsCommandPlayerInfo) {
            required = true
        }
    }
}