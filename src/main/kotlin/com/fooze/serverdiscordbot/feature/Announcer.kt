package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.ServerDiscordBot
import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.config.StreakHandler
import com.fooze.serverdiscordbot.util.Colors
import com.fooze.serverdiscordbot.util.Format
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.stat.Stats
import org.slf4j.Logger

object Announcer {
    fun load(scope: CoroutineScope, bot: Kord?, config: ModConfig, lang: LangConfig, logger: Logger) {
        // On player join
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val name = handler.player.name.string
            val streak = StreakHandler.getStreak(name)

            // Placeholders
            val placeholders = mapOf(
                "player" to Format.escape(name),
                "streak" to Format.number(streak.count)
            )

            // Include streak description if applicable
            val description = if (streak.count > 1 && streak.updated) {
                Format.replace(lang.announceJoinDescription, placeholders)
            } else {
                null
            }

            val message = Format.replace(lang.announceJoin, placeholders)

            // Send join announcement and update presence
            scope.launch {
                announcePlayerEvent(bot, config, lang, logger, description, Colors.GREEN, message, name)
                updatePresence(bot, lang, handler.player.entityWorld.server)
            }
        }

        // On player leave
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            // Skip if the server is stopping
            if (ServerDiscordBot.stopping) return@register

            val name = handler.player.name.string

            // Placeholders
            val placeholders = mapOf("player" to Format.escape(name))

            val message = Format.replace(lang.announceLeave, placeholders)

            // Send leave announcement and update presence
            scope.launch {
                announcePlayerEvent(bot, config, lang, logger, null, Colors.RED, message, name)
                updatePresence(bot, lang, handler.player.entityWorld.server)
            }
        }

        // On player death
        ServerLivingEntityEvents.AFTER_DEATH.register { entity, damageSource ->
            if (entity is ServerPlayerEntity) {
                val name = entity.name.string
                val deaths = entity.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS))

                // Placeholders
                val placeholders = mapOf("deaths" to Format.number(deaths))

                val description = Format.replace(lang.announceDeathDescription, placeholders)
                val message = Format.escape(damageSource.getDeathMessage(entity).string)

                // Send death announcement
                scope.launch {
                    announcePlayerEvent(bot, config, lang, logger, description, Colors.RED, message, name)
                }
            }
        }
    }

    // Creates an announcement and sends it to the configured channel
    private suspend fun announce(
        bot: Kord?,
        config: ModConfig,
        lang: LangConfig,
        logger: Logger,
        embed: EmbedBuilder.() -> Unit
    ) {
        runCatching {
            bot?.getChannelOf<TextChannel>(Snowflake(config.discordChannelId))?.createEmbed(embed)
        }.onFailure {
            logger.error(lang.logAnnounceFail, it)
        }
    }

    // Announces a server event
    suspend fun announceServerEvent(
        bot: Kord?,
        config: ModConfig,
        lang: LangConfig,
        logger: Logger,
        title: String,
        description: String,
        color: Color
    ) {
        announce(bot, config, lang, logger) {
            // Placeholders
            val placeholders = mapOf(
                "server" to Format.serverName(config, lang, true),
                "ip" to config.serverIp
            )

            // Build the embed
            this.title = title
            this.description = Format.replace(description, placeholders)
            this.color = color
        }
    }

    // Announces a player event
    suspend fun announcePlayerEvent(
        bot: Kord?,
        config: ModConfig,
        lang: LangConfig,
        logger: Logger,
        description: String?,
        color: Color,
        message: String,
        player: String
    ) {
        announce(bot, config, lang, logger) {
            // Build the embed
            this.description = description
            this.color = color

            author {
                this.name = message
                icon = "https://mc-heads.net/avatar/${player}"
            }
        }
    }

    // Updates the presence to show the current player count
    private suspend fun updatePresence(bot: Kord?, lang: LangConfig, server: MinecraftServer) {
        val count = server.playerManager.currentPlayerCount

        // Use a plural if there are multiple players online
        val template = if (count == 1) {
            lang.announcePresence
        } else {
            lang.announcePresencePlural
        }

        // Placeholders
        val placeholders = mapOf("count" to Format.number(count))

        // Only update presence when there are players online
        if (count > 0) {
            bot?.editPresence { watching(Format.replace(template, placeholders)) }
        } else {
            bot?.editPresence { toPresence() }
        }
    }
}