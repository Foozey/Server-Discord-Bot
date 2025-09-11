package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.config.ModConfig
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
    fun load(scope: CoroutineScope, kord: Kord, config: ModConfig, logger: Logger) {
        suspend fun announce(block: EmbedBuilder.() -> Unit) = runCatching {
            kord.getChannelOf<TextChannel>(Snowflake(config.channelId))?.createEmbed(block)
        }.onFailure {
            logger.error("Failed to send announcement", it)
        }

        fun announcePlayerEvent(name: String, message: String, color: Color, description: String? = null) {
            scope.launch {
                announce {
                    author {
                        this.name = "$name $message"
                        icon = "https://mc-heads.net/avatar/$name"
                    }

                    this.color = color
                    this.description = description
                }
            }
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val name = handler.player.name.string
            announcePlayerEvent(name, "joined the game", Color(0x00FF00))
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            val name = handler.player.name.string
            announcePlayerEvent(name, "left the game", Color(0xFF0000))
        }

        ServerLivingEntityEvents.AFTER_DEATH.register { entity, _ ->
            if (entity is ServerPlayerEntity) {
                val name = entity.name.string
                val deaths = entity.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS))
                announcePlayerEvent(name, "died", Color(0xFF0000), "Total deaths: $deaths")
            }
        }
    }
}