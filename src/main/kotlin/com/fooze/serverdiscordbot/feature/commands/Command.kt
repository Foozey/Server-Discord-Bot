package com.fooze.serverdiscordbot.feature.commands

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Format
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger

abstract class Command(
    private val name: (LangConfig) -> String,
    private val description: (LangConfig) -> String
) {
    var command: GuildChatInputCommand? = null

    suspend fun load(
        logger: Logger,
        bot: Kord?,
        server: MinecraftServer?,
        config: ModConfig,
        lang: LangConfig
    ) {
        if (bot == null) return

        // Create the command
        runCatching {
            val channel = bot.getChannelOf<TextChannel>(Snowflake(config.discordChannelId)) ?: return

            command = bot.createGuildChatInputCommand(channel.guildId, name(lang), description(lang)) {
                options(lang, this)
            }
        }.onFailure {
            // Placeholders
            val placeholders = mapOf("command" to name(lang))

            logger.error(Format.replace(lang.logCommandFail, placeholders))
        }

        // Create the interaction
        bot.on<GuildChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != command?.name) return@on
            run(server, config, lang, this)
        }
    }

    // The options the command can use (e.g., /whitelist <player>)
    open suspend fun options(lang: LangConfig, builder: ChatInputCreateBuilder) {}

    // The interaction to run when the command is used
    abstract suspend fun run(
        server: MinecraftServer?,
        config: ModConfig,
        lang: LangConfig,
        event: GuildChatInputCommandInteractionCreateEvent,
    )
}