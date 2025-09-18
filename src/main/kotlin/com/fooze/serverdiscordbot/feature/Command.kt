package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Placeholder
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

    // Load the command into the bot
    suspend fun load(
        bot: Kord?,
        config: ModConfig,
        lang: LangConfig,
        logger: Logger,
        server: MinecraftServer? = null
    ) {
        // Create the command
        command = runCatching {
            val channel = bot?.getChannelOf<TextChannel>(Snowflake(config.discordChannelId)) ?: return

            bot.createGuildChatInputCommand(channel.guildId, name(lang), description(lang)) {
                options(this, lang)
            }
        }.onFailure {
            val values = mapOf("command" to name(lang))
            logger.error(Placeholder.replace(lang.logCommandFail, values), it)
        }.getOrNull() ?: return

        // Create the interaction
        bot?.on<GuildChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != command?.name) return@on
            run(this, config, lang, server)
        }
    }

    // The options the command can use (e.g., /whitelist <player>)
    open suspend fun options(builder: ChatInputCreateBuilder, lang: LangConfig) {}

    // The interaction to run when the command is used
    abstract suspend fun run(
        event: GuildChatInputCommandInteractionCreateEvent,
        config: ModConfig,
        lang: LangConfig,
        server: MinecraftServer? = null
    )
}