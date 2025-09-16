package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Colors
import com.fooze.serverdiscordbot.util.Format
import com.fooze.serverdiscordbot.util.Placeholder
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
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.stat.Stats
import org.slf4j.Logger

object Announcer {
    private suspend fun announce(
        bot: Kord,
        config: ModConfig,
        lang: LangConfig,
        logger: Logger,
        embed: EmbedBuilder.() -> Unit
    ) {
        runCatching {
            bot.getChannelOf<TextChannel>(Snowflake(config.channelId))?.createEmbed(embed)
        }.onFailure {
            logger.error(lang.logAnnounceFail, it)
        }
    }

    suspend fun announceServerEvent(
        bot: Kord,
        config: ModConfig,
        lang: LangConfig,
        logger: Logger,
        title: String,
        description: String,
        color: Color
    ) {
        announce(bot, config, lang, logger) {
            // Placeholders
            val values = mapOf(
                "server" to Format.serverName(config, lang, true),
                "ip" to config.serverIp
            )

            // Embed formatting
            this.title = title
            this.description = Placeholder.replace(description, values)
            this.color = color
        }
    }

    private fun announcePlayerEvent(
        scope: CoroutineScope,
        bot: Kord,
        config: ModConfig,
        lang: LangConfig,
        logger: Logger,
        player: String,
        message: String,
        description: String?,
        color: Color
    ) {
        scope.launch {
            announce(bot, config, lang, logger) {
                // Embed formatting
                author {
                    this.name = message
                    icon = "https://mc-heads.net/avatar/$player"
                }

                this.description = description
                this.color = color
            }
        }
    }

    fun load(scope: CoroutineScope, bot: Kord, config: ModConfig, lang: LangConfig, logger: Logger) {
        // On player join
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val player = handler.player.name.string

            // Placeholders
            val values = mapOf("player" to player)
            val message = Placeholder.replace(lang.announceJoin, values)

            announcePlayerEvent(scope, bot, config, lang, logger, player, message, null, Colors.GREEN)
        }

        // On player leave
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            val player = handler.player.name.string

            // Placeholders
            val values = mapOf("player" to player)
            val message = Placeholder.replace(lang.announceLeave, values)

            announcePlayerEvent(scope, bot, config, lang, logger, player, message, null, Colors.RED)
        }

        // On player death
        ServerLivingEntityEvents.AFTER_DEATH.register { entity, _ ->
            if (entity is ServerPlayerEntity) {
                val player = entity.name.string

                // Placeholders
                val values = mapOf(
                    "player" to player,
                    "deaths" to entity.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS)).toString()
                )

                val message = Placeholder.replace(lang.announceDeath, values)
                val description = Placeholder.replace(lang.announceDeathTotal, values)

                announcePlayerEvent(scope, bot, config, lang, logger, player, message, description, Colors.RED)
            }
        }
    }
}