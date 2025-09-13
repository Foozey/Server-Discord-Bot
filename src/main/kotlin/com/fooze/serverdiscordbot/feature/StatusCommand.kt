package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.ServerDiscordBot
import com.sun.management.OperatingSystemMXBean
import dev.kord.common.Color
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.minecraft.server.MinecraftServer
import java.lang.management.ManagementFactory
import java.time.Instant

object StatusCommand {
    private fun getTicks(server: MinecraftServer, isTps: Boolean): String {
        val mspt = if (server.tickTimes.isNotEmpty()) server.tickTimes.average() / 1.0e6 else 0.0
        val tps = if (mspt > 0) (1000.0 / mspt).coerceAtMost(20.0) else 20.0
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
        val total = runtime.totalMemory() / 1024 / 1024
        return "$used MB / $total MB"
    }

    private fun getPlayerCount(server: MinecraftServer): String {
        return "${server.playerManager.currentPlayerCount} / ${server.playerManager.maxPlayerCount}"
    }

    private fun getPlayerList(server: MinecraftServer): String {
        val players = server.playerManager.playerList
        val maxSize = 10
        val remaining = players.size - maxSize
        if (players.isEmpty()) return ">>> No players online"

        return buildString {
            appendLine(">>> ${players.take(maxSize).joinToString("\n") { it.name.string }}")
            if (remaining > 0) append("...and $remaining more")
        }
    }

    private fun EmbedBuilder.systemField(name: String, value: String?) {
        field(name, true) { value?.let { "```$it```" } ?: "N/A" }
    }

    fun load(scope: CoroutineScope, kord: Kord) {
        scope.launch {
            val statusCommand = kord.createGlobalChatInputCommand("status", "Displays the server status")

            kord.on<GuildChatInputCommandInteractionCreateEvent> {
                if (interaction.command.rootName != statusCommand.name) return@on
                val server = ServerDiscordBot.minecraftServer

                interaction.deferPublicResponse().respond {
                    embed {
                        title = "Server Status"

                        systemField("State", server?.let { "\uD83D\uDFE2 Online" } ?: "\uD83D\uDD34 Offline")
                        systemField("TPS", server?.let { getTicks(it, true) })
                        systemField("MSPT", server?.let { getTicks(it, false) })
                        systemField("CPU Usage", server?.let { getCpuUsage() })
                        systemField("RAM Usage", server?.let { getRamUsage() })

                        field("")

                        field("Players (${server?.let { getPlayerCount(it) } ?: "N/A"})") {
                            server?.let { getPlayerList(it) } ?: "N/A"
                        }

                        field("")

                        field("") {
                            "-# Last updated <t:${Instant.now().epochSecond}:R>, use </status:${statusCommand.id}> to update"
                        }

                        color = Color(0xFFFF00)
                    }
                }
            }
        }
    }
}