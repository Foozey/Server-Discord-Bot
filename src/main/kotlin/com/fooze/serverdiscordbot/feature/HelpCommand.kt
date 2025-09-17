package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Format
import com.fooze.serverdiscordbot.util.Placeholder
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.embed
import org.slf4j.Logger

object HelpCommand {
    suspend fun load(bot: Kord?, config: ModConfig, lang: LangConfig, logger: Logger) {
        // Create the command
        val helpCommand = runCatching {
            val channel = bot?.getChannelOf<TextChannel>(Snowflake(config.discordChannelId)) ?: return
            bot.createGuildChatInputCommand(channel.guildId, lang.helpCommand, lang.helpCommandInfo)
        }.onFailure {
            logger.error(lang.logStatusFail, it)
        }.getOrNull() ?: return

        // Create the interaction
        bot?.on<GuildChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != helpCommand.name) return@on

            // Placeholders
            val values = mapOf(
                "server" to Format.serverName(config, lang, false),
                "status" to lang.statusCommand,
                "statusId" to StatusCommand.statusCommand?.id.toString(),
                "whitelist" to lang.whitelistCommand,
                "whitelistId" to WhitelistCommand.whitelistCommand?.id.toString()
            )

            // Build the embed
            interaction.deferPublicResponse().respond {
                embed {
                    title = lang.helpTitle
                    description = Placeholder.replace(lang.helpDescription, values)

                    field(Placeholder.replace(lang.helpStatusTitle, values)) {
                        lang.helpStatusDescription
                    }

                    field(Placeholder.replace(lang.helpWhitelistTitle, values)) {
                        lang.helpWhitelistDescription
                    }
                }
            }
        }
    }
}