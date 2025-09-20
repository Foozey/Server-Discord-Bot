package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Format
import com.fooze.serverdiscordbot.util.Placeholder
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import net.minecraft.server.MinecraftServer

object HelpCommand : Command({ it.helpCommand }, { it.helpCommandInfo }) {
    override suspend fun run(
        event: GuildChatInputCommandInteractionCreateEvent,
        config: ModConfig,
        lang: LangConfig,
        server: MinecraftServer?
    ) {
        // Placeholders
        val values = mapOf(
            "server" to Format.serverName(config, lang, false),
            "help" to lang.helpCommand,
            "helpId" to command?.id.toString(),
            "status" to lang.statusCommand,
            "statusId" to StatusCommand.command?.id.toString(),
            "whitelist" to lang.whitelistCommand,
            "whitelistId" to WhitelistCommand.command?.id.toString(),
            "stats" to lang.statsCommand,
            "statsId" to StatsCommand.command?.id.toString()
        )

        // Build the embed
        event.interaction.deferPublicResponse().respond {
            embed {
                title = lang.helpTitle
                description = Placeholder.replace(lang.helpDescription, values)

                // Commands
                field("")
                field("") { Placeholder.replace(lang.helpHelp, values) }
                field("") { Placeholder.replace(lang.helpStatus, values) }

                field("") {
                    "${Placeholder.replace(lang.helpWhitelist, values)}\n" +
                            "-# ${Placeholder.replace(lang.helpWhitelistUsage, values)}"
                }

                field("") {
                    "${Placeholder.replace(lang.helpStats, values)}\n" +
                            "-# ${Placeholder.replace(lang.helpStatsUsage, values)}"
                }
            }
        }
    }
}