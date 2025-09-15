package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.ServerDiscordBot
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.config.ServerType
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.minecraft.server.WhitelistEntry
import org.slf4j.Logger

object WhitelistCommand {
    fun load(scope: CoroutineScope, kord: Kord?, config: ModConfig, logger: Logger) {
        scope.launch {
            val whitelistCommand = runCatching {
                val channel = kord?.getChannelOf<TextChannel>(Snowflake(config.channelId)) ?: return@launch

                kord.createGuildChatInputCommand(channel.guildId, "whitelist", "Adds a player to the whitelist") {
                    string("player", "The player to whitelist") {
                        required = true
                    }
                }
            }.onFailure {
                logger.error("Whitelist command failed to initialize! Your channel id may be invalid", it)
            }.getOrNull() ?: return@launch

            kord?.on<GuildChatInputCommandInteractionCreateEvent> {
                if (interaction.command.rootName != whitelistCommand.name) return@on
                val server = ServerDiscordBot.minecraftServer ?: return@on
                val serverType = ServerType.getServerType(config, server)
                val player = interaction.command.strings["player"]
                val profile = server.gameProfileRepo.findProfileByName(player).orElse(null)

                interaction.deferPublicResponse().respond {
                    embed {
                        if (profile == null) {
                            description = "**$player** not found! The name may be invalid"
                            color = Color(0xEF5350)
                        } else if (server.playerManager.whitelist.isAllowed(profile)) {
                            description = "**$player** is already whitelisted"
                            color = Color(0xFF9800)
                        } else {
                            server.playerManager.whitelist.add(WhitelistEntry(profile))
                            title = "Added $player to the whitelist"
                            color = Color(0x4CAF50)
                            thumbnail { url = "https://mc-heads.net/avatar/$player" }

                            field("Welcome to the server $player!") {
                                "To join, make sure you're playing **${serverType}** and connect using the IP below:"
                            }

                            field("") { if (config.serverIp.isNotEmpty()) "```${config.serverIp}```" else "```N/A```" }
                        }
                    }
                }
            }
        }
    }
}