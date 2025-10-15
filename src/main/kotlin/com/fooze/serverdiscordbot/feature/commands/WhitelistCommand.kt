package com.fooze.serverdiscordbot.feature.commands

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Colors
import com.fooze.serverdiscordbot.util.Format
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import net.minecraft.server.MinecraftServer
import net.minecraft.server.PlayerConfigEntry
import net.minecraft.server.WhitelistEntry

object WhitelistCommand : Command({ it.whitelistCommand }, { it.whitelistCommandInfo }) {
    override suspend fun run(
        config: ModConfig,
        lang: LangConfig,
        server: MinecraftServer?,
        event: GuildChatInputCommandInteractionCreateEvent
    ) {
        if (server == null) return

        val response = event.interaction.deferPublicResponse()
        val name = event.interaction.command.strings[lang.whitelistCommandPlayer]
        val profile = server.apiServices.profileResolver.getProfileByName(name).orElse(null)

        // Placeholders
        val placeholders = mapOf(
            "server" to Format.serverName(config, lang, false),
            "type" to Format.serverType(config, lang, server),
            "player" to Format.escape(name.toString())
        )

        // Build the embed
        response.respond {
            embed {
                // If the profile is invalid, send an error message
                if (profile == null) {
                    description = Format.replace(lang.whitelistInvalid, placeholders)
                    color = Colors.RED
                }

                // If the player is already whitelisted, send a warning message
                else if (server.playerManager.whitelist.isAllowed(PlayerConfigEntry(profile))) {
                    description = Format.replace(lang.whitelistExisting, placeholders)
                    color = Colors.YELLOW
                }

                // Add the player to the whitelist and send a success message
                else {
                    server.playerManager.whitelist.add(WhitelistEntry(PlayerConfigEntry(profile)))

                    title = Format.replace(lang.whitelistAddTitle, placeholders)
                    description = Format.replace(lang.whitelistAddDescription, placeholders)
                    color = Colors.GREEN
                    thumbnail { url = "https://mc-heads.net/avatar/${name}" }

                    // Secondary description
                    field("") { Format.replace(lang.whitelistAddDescriptionInfo, placeholders) }

                    // Server IP
                    field("") {
                        if (config.serverIp.isNotBlank()) {
                            "```${config.serverIp}```"
                        } else {
                            "```${lang.whitelistIpMissing}```"
                        }
                    }
                }
            }
        }
    }

    // Defines the player as a required command option
    override suspend fun options(builder: ChatInputCreateBuilder, lang: LangConfig) {
        builder.string(lang.whitelistCommandPlayer, lang.whitelistCommandPlayerInfo) {
            required = true
        }
    }
}