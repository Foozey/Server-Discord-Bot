package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.config.ServerType
import com.fooze.serverdiscordbot.util.Colors
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
    private suspend fun announce(kord: Kord, config: ModConfig, logger: Logger, block: EmbedBuilder.() -> Unit) {
        runCatching {
            kord.getChannelOf<TextChannel>(Snowflake(config.channelId))?.createEmbed(block)
        }.onFailure {
            logger.error("Announcement failed! Your channel ID may be invalid", it)
        }
    }

    suspend fun announceServerEvent(kord: Kord, config: ModConfig, logger: Logger, state: String, message: String, color: Color) {
        announce(kord, config, logger) {
            val serverName = ServerType.getServerName(config, true)
            title = "Server $state!"
            description = "$serverName is now $message"
            this.color = color
        }
    }

    private fun announcePlayerEvent(scope: CoroutineScope, kord: Kord, config: ModConfig, logger: Logger, name: String, message: String, description: String?, color: Color) {
        scope.launch {
            announce(kord, config, logger) {
                author {
                    this.name = "$name $message"
                    icon = "https://mc-heads.net/avatar/$name"
                }

                this.description = description
                this.color = color
            }
        }
    }

    fun load(scope: CoroutineScope, kord: Kord, config: ModConfig, logger: Logger) {
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val name = handler.player.name.string
            announcePlayerEvent(scope, kord, config, logger, name, "joined the game", null, Colors.GREEN)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            val name = handler.player.name.string
            announcePlayerEvent(scope, kord, config, logger, name, "left the game", null, Colors.RED)
        }

        ServerLivingEntityEvents.AFTER_DEATH.register { entity, _ ->
            if (entity is ServerPlayerEntity) {
                val name = entity.name.string
                val deaths = entity.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS))
                announcePlayerEvent(scope, kord, config, logger, name, "died", "Total deaths: $deaths", Colors.RED)
            }
        }
    }
}