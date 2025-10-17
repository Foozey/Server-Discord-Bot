package com.fooze.serverdiscordbot.feature.commands

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Format
import com.sun.management.OperatingSystemMXBean
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import net.minecraft.server.MinecraftServer
import java.lang.management.ManagementFactory
import java.time.Instant

object StatusCommand : Command({ it.statusCommand }, { it.statusCommandInfo }) {
    override suspend fun run(
        server: MinecraftServer?,
        config: ModConfig,
        lang: LangConfig,
        event: GuildChatInputCommandInteractionCreateEvent
    ) {
        if (server == null) return

        val response = event.interaction.deferPublicResponse()

        // Placeholders
        val placeholders = mapOf(
            "server" to Format.serverName(config, lang, false),
            "count" to getPlayerCount(server),
            "time" to Instant.now().epochSecond.toString(),
            "status" to lang.statusCommand,
            "id" to command?.id.toString()
        )

        // Build the embed
        response.respond {
            embed {
                title = lang.statusTitle
                description = Format.replace(lang.statusDescription, placeholders)

                // System fields
                field("")
                field(lang.statusState, true) { "```\uD83D\uDFE2 ${lang.statusStateValue}```" }
                field(lang.statusTps, true) { "```${Format.decimal(getTps(server))} ticks```" }
                field(lang.statusMspt, true) { "```${Format.decimal(getMspt(server))} ms```" }
                field(lang.statusCpu, true) { "```${Format.decimal(getCpuUsage())}%```" }
                field(lang.statusRam, true) { "```${getRamUsage()}```" }
                field("")

                // Player list
                field(Format.replace(lang.statusPlayers, placeholders)) { getPlayerList(server, lang) }
                field("")

                // Last updated footer
                field("") { "-# ${Format.replace(lang.statusUpdate, placeholders)}" }
            }
        }
    }

    // Returns the server's TPS
    private fun getTps(server: MinecraftServer): Double {
        return (1000.0 / getMspt(server)).coerceAtMost(20.0)
    }

    // Returns the server's MSPT
    private fun getMspt(server: MinecraftServer): Double {
        return server.tickTimes.average() / 1.0E6
    }

    // Returns the server's CPU usage
    private fun getCpuUsage(): Double {
        val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        return osBean.processCpuLoad * 100
    }

    // Returns the server's RAM usage in used MB / max MB
    private fun getRamUsage(): String {
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val max = runtime.maxMemory() / 1024 / 1024
        return "${Format.number(used)} MB / ${Format.number(max)} MB"
    }

    // Returns the server's player count in online / max
    private fun getPlayerCount(server: MinecraftServer): String {
        val current = server.playerManager.currentPlayerCount
        val max = server.playerManager.maxPlayerCount
        return "${Format.number(current)} / ${Format.number(max)}"
    }

    // Returns the server's player list up to 20 players, then counts the remaining
    private fun getPlayerList(server: MinecraftServer, lang: LangConfig): String {
        val players = server.playerManager.playerList
        val max = 20
        val remaining = players.size - max

        // Show a message if the server is empty
        if (players.isEmpty()) {
            return ">>> ${lang.statusPlayersNone}"
        }

        // Placeholders
        val placeholders = mapOf("remaining" to Format.number(remaining))

        return buildString {
            appendLine(">>> ${players.take(max).joinToString("\n") { Format.escape(it.name.string) }}")

            // Append a message if there are more players than the max
            if (remaining > 0) {
                append(Format.replace(lang.statusPlayersMore, placeholders))
            }
        }
    }
}