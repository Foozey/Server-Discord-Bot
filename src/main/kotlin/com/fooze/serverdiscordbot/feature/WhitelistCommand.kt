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
import net.minecraft.server.MinecraftServer
import net.minecraft.server.PlayerConfigEntry
import net.minecraft.server.WhitelistEntry

object WhitelistCommand : Command({ it.whitelistCommand }, { it.whitelistCommandInfo }) {
    override suspend fun run(
        event: GuildChatInputCommandInteractionCreateEvent,
        config: ModConfig,
        lang: LangConfig,
        server: MinecraftServer?
    ) {
        if (server == null) return

        val name = event.interaction.command.strings[lang.whitelistCommandPlayer]
        val profile = server.apiServices.profileRepository.findProfileByName(name).orElse(null)

        // Placeholders
        val values = mapOf(
            "server" to Format.serverName(config, lang, false),
            "type" to Format.serverType(config, lang, server),
            "player" to name.toString()
        )

        // Build the embed
        event.interaction.deferPublicResponse().respond {
            embed {
                // If the profile is invalid, send an error message
                if (profile == null) {
                    description = Placeholder.replace(lang.whitelistInvalid, values)
                    color = Colors.RED
                }

                // If the player is already whitelisted, send a warning message
                else if (server.playerManager.whitelist.isAllowed(PlayerConfigEntry(profile))) {
                    description = Placeholder.replace(lang.whitelistExisting, values)
                    color = Colors.YELLOW
                }

                // Add the player to the whitelist and send a success message
                else {
                    server.playerManager.whitelist.add(WhitelistEntry(PlayerConfigEntry(profile)))

                    title = Placeholder.replace(lang.whitelistAddTitle, values)
                    description = Placeholder.replace(lang.whitelistAddDescription, values)
                    color = Colors.GREEN
                    thumbnail { url = "https://mc-heads.net/avatar/${name}" }

                    // Secondary description
                    field("") { Placeholder.replace(lang.whitelistAddDescriptionInfo, values) }

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