package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.ServerDiscordBot
import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Colors
import com.fooze.serverdiscordbot.util.Format
import com.fooze.serverdiscordbot.util.Placeholder
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import net.minecraft.server.WhitelistEntry
import org.slf4j.Logger

object WhitelistCommand {
    suspend fun load(bot: Kord, config: ModConfig, lang: LangConfig, logger: Logger) {
        // Create the command
        val whitelistCommand = runCatching {
            val channel = bot.getChannelOf<TextChannel>(Snowflake(config.channelId)) ?: return

            bot.createGuildChatInputCommand(channel.guildId, lang.whitelistCommand, lang.whitelistCommandInfo) {
                string(lang.whitelistCommandPlayer, lang.whitelistCommandPlayerInfo) {
                    required = true
                }
            }
        }.onFailure {
            logger.error(lang.logWhitelistFail, it)
        }.getOrNull() ?: return

        // Create the interaction
        bot.on<GuildChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != whitelistCommand.name) return@on
            val server = ServerDiscordBot.server
            val player = interaction.command.strings[lang.whitelistCommandPlayer]
            val profile = server.gameProfileRepo.findProfileByName(player).orElse(null)

            // Placeholders
            val values = mapOf(
                "server" to Format.serverName(config, lang, false),
                "type" to Format.serverType(config, lang, server),
                "player" to player.toString()
            )

            // Embed formatting
            interaction.deferPublicResponse().respond {
                embed {
                    if (profile == null) {
                        description = Placeholder.replace(lang.whitelistInvalid, values)
                        color = Colors.RED
                    } else if (server.playerManager.whitelist.isAllowed(profile)) {
                        description = Placeholder.replace(lang.whitelistExisting, values)
                        color = Colors.YELLOW
                    } else {
                        server.playerManager.whitelist.add(WhitelistEntry(profile))
                        title = Placeholder.replace(lang.whitelistAdd, values)
                        color = Colors.GREEN
                        thumbnail { url = "https://mc-heads.net/avatar/$player" }

                        field(Placeholder.replace(lang.whitelistAddTitle, values)) {
                            Placeholder.replace(lang.whitelistAddDescription, values)
                        }

                        field("") {
                            if (config.serverIp.isNotEmpty()) {
                                "```${config.serverIp}```"
                            } else {
                                "```${lang.whitelistIpMissing}```"
                            }
                        }
                    }
                }
            }
        }
    }
}