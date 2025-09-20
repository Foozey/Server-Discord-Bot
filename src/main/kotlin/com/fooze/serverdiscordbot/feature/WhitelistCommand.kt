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
        val profile = server.gameProfileRepo.findProfileByName(name).orElse(null)

        // Placeholders
        val values = mapOf(
            "server" to Format.serverName(config, lang, false),
            "type" to Format.serverType(config, lang, server),
            "player" to name.toString()
        )

        // Build the embed
        event.interaction.deferPublicResponse().respond {
            embed {
                if (profile == null) {
                    description = Placeholder.replace(lang.whitelistInvalid, values)
                    color = Colors.RED
                } else if (server.playerManager.whitelist.isAllowed(profile)) {
                    description = Placeholder.replace(lang.whitelistExisting, values)
                    color = Colors.YELLOW
                } else {
                    // Add the player to the whitelist
                    server.playerManager.whitelist.add(WhitelistEntry(profile))

                    title = Placeholder.replace(lang.whitelistAddTitle, values)
                    description = Placeholder.replace(lang.whitelistAddDescription, values)
                    color = Colors.GREEN
                    thumbnail { url = "https://mc-heads.net/avatar/${name}" }

                    // Joining info
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