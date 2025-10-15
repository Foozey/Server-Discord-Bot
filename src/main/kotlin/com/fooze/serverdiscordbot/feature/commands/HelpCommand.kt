package com.fooze.serverdiscordbot.feature.commands

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Format
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import net.minecraft.server.MinecraftServer

object HelpCommand : Command({ it.helpCommand }, { it.helpCommandInfo }) {
    override suspend fun run(
        config: ModConfig,
        lang: LangConfig,
        server: MinecraftServer?,
        event: GuildChatInputCommandInteractionCreateEvent
    ) {
        val response = event.interaction.deferPublicResponse()

        // Placeholders
        val placeholders = mapOf(
            "server" to Format.serverName(config, lang, false),
            "help" to lang.helpCommand,
            "helpId" to command?.id.toString(),
            "status" to lang.statusCommand,
            "statusId" to StatusCommand.command?.id.toString(),
            "whitelist" to lang.whitelistCommand,
            "whitelistId" to WhitelistCommand.command?.id.toString(),
            "stats" to lang.statsCommand,
            "statsId" to StatsCommand.command?.id.toString(),
            "leaderboard" to lang.leaderboardCommand,
            "leaderboardId" to LeaderboardCommand.command?.id.toString()
        )

        // Build the embed
        response.respond {
            embed {
                title = lang.helpTitle
                description = Format.replace(lang.helpDescription, placeholders)
                field("")

                // Help Command
                field("") { Format.replace(lang.helpHelp, placeholders) }

                // Status Command
                field("") { Format.replace(lang.helpStatus, placeholders) }

                // Whitelist Command
                field("") {
                    "${Format.replace(lang.helpWhitelist, placeholders)}\n" +
                            "-# ${Format.replace(lang.helpWhitelistUsage, placeholders)}"
                }

                // Stats Command
                field("") {
                    "${Format.replace(lang.helpStats, placeholders)}\n" +
                            "-# ${Format.replace(lang.helpStatsUsage, placeholders)}"
                }

                // Leaderboard Command
                field("") {
                    "${Format.replace(lang.helpLeaderboard, placeholders)}\n" +
                            "-# ${Format.replace(lang.helpLeaderboardUsage, placeholders)}"
                }
            }
        }
    }
}