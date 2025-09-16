package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.ServerDiscordBot
import com.fooze.serverdiscordbot.config.ModConfig
import com.sun.management.OperatingSystemMXBean
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.embed
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import java.lang.management.ManagementFactory
import java.time.Instant

object StatusCommand {
    private fun getTicks(server: MinecraftServer, isTps: Boolean): String {
        val mspt = server.tickTimes.average() / 1.0E6
        val tps = (1000.0 / mspt).coerceAtMost(20.0)
        return String.format("%.1f", if (isTps) tps else mspt)
    }

    private fun getCpuUsage(): String {
        val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        val cpuLoad = osBean.processCpuLoad * 100
        return String.format("%.1f", cpuLoad) + "%"
    }

    private fun getRamUsage(): String {
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val max = runtime.maxMemory() / 1024 / 1024
        return "$used MB / $max MB"
    }

    private fun getPlayerCount(server: MinecraftServer): String {
        return "${server.playerManager.currentPlayerCount} / ${server.playerManager.maxPlayerCount}"
    }

    private fun getPlayerList(server: MinecraftServer): String {
        val players = server.playerManager.playerList
        if (players.isEmpty()) return ">>> No players online"
        val maxSize = 10
        val remaining = players.size - maxSize

        return buildString {
            appendLine(">>> ${players.take(maxSize).joinToString("\n") { it.name.string }}")
            if (remaining > 0) append("...and $remaining more")
        }
    }

    suspend fun load(kord: Kord, config: ModConfig, logger: Logger) {
        val statusCommand = runCatching {
            val channel = kord.getChannelOf<TextChannel>(Snowflake(config.channelId)) ?: return
            kord.createGuildChatInputCommand(channel.guildId, "status", "Displays the server status")
        }.onFailure {
            logger.error("Status command failed to initialize! Your channel ID may be invalid", it)
        }.getOrNull() ?: return

        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != statusCommand.name) return@on
            val server = ServerDiscordBot.server

            interaction.deferPublicResponse().respond {
                embed {
                    title = "Server Status"
                    field("State", true) { "```\uD83D\uDFE2 Online```" }
                    field("TPS", true) { "```${getTicks(server, true)}```" }
                    field("MSPT", true) { "```${getTicks(server, false)}```" }
                    field("CPU Usage", true) { "```${getCpuUsage()}```" }
                    field("RAM Usage", true) { "```${getRamUsage()}```" }
                    field("")
                    field("Players (${getPlayerCount(server)})") { getPlayerList(server) }
                    field("")
                    field("") { "-# Last updated <t:${Instant.now().epochSecond}:R>, use </status:${statusCommand.id}> to update" }
                }
            }
        }
    }
}