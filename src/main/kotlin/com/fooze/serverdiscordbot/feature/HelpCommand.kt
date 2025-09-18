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
            "status" to lang.statusCommand,
            "statusId" to StatusCommand.command?.id.toString(),
            "whitelist" to lang.whitelistCommand,
            "whitelistId" to WhitelistCommand.command?.id.toString(),
        )

        // Build the embed
        event.interaction.deferPublicResponse().respond {
            embed {
                title = lang.helpTitle
                description = Placeholder.replace(lang.helpDescription, values)

                field(Placeholder.replace(lang.helpStatusTitle, values)) {
                    lang.helpStatusDescription
                }

                field(Placeholder.replace(lang.helpWhitelistTitle, values)) {
                    Placeholder.replace(lang.helpWhitelistDescription, values)
                }
            }
        }
    }
}