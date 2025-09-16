package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.ServerDiscordBot
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.config.ServerType
import com.fooze.serverdiscordbot.util.Colors
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
    suspend fun load(kord: Kord, config: ModConfig, logger: Logger) {
        val whitelistCommand = runCatching {
            val channel = kord.getChannelOf<TextChannel>(Snowflake(config.channelId)) ?: return

            kord.createGuildChatInputCommand(channel.guildId, "whitelist", "Adds a player to the whitelist") {
                string("player", "The player to whitelist") {
                    required = true
                }
            }
        }.onFailure {
            logger.error("Whitelist command failed to initialize! Your channel id may be invalid", it)
        }.getOrNull() ?: return

        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != whitelistCommand.name) return@on
            val server = ServerDiscordBot.server
            val serverName = ServerType.getServerName(config)
            val serverType = ServerType.getServerType(config, server)
            val player = interaction.command.strings["player"]
            val profile = server.gameProfileRepo.findProfileByName(player).orElse(null)

            interaction.deferPublicResponse().respond {
                embed {
                    if (profile == null) {
                        description = "**$player** not found! The name may be invalid"
                        color = Colors.RED
                    } else if (server.playerManager.whitelist.isAllowed(profile)) {
                        description = "**$player** is already whitelisted"
                        color = Colors.YELLOW
                    } else {
                        server.playerManager.whitelist.add(WhitelistEntry(profile))
                        title = "Added $player to the whitelist"
                        color = Colors.GREEN
                        thumbnail { url = "https://mc-heads.net/avatar/$player" }

                        field("Welcome to $serverName!") {
                            "To join, make sure you're playing **${serverType}**, and connect using the IP below:"
                        }

                        field("") {
                            if (config.serverIp.isNotEmpty()) "```${config.serverIp}```" else "```N/A```"
                        }
                    }
                }
            }
        }
    }
}