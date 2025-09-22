package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Format
import com.fooze.serverdiscordbot.util.Placeholder
import com.sun.management.OperatingSystemMXBean
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import net.minecraft.server.MinecraftServer
import java.lang.management.ManagementFactory
import java.time.Instant

object StatusCommand : Command({ it.statusCommand }, { it.statusCommandInfo }) {
    override suspend fun run(
        event: GuildChatInputCommandInteractionCreateEvent,
        config: ModConfig,
        lang: LangConfig,
        server: MinecraftServer?
    ) {
        if (server == null) return

        // Placeholders
        val values = mapOf(
            "server" to Format.serverName(config, lang, false),
            "count" to getPlayerCount(server),
            "time" to Instant.now().epochSecond.toString(),
            "status" to lang.statusCommand,
            "id" to command?.id.toString()
        )

        // Build the embed
        event.interaction.deferPublicResponse().respond {
            embed {
                title = lang.statusTitle
                description = Placeholder.replace(lang.statusDescription, values)

                // System fields
                field("")
                field(lang.statusState, true) { "```\uD83D\uDFE2 ${lang.statusStateValue}```" }
                field(lang.statusTps, true) { "```${getTicks(server, true)}```" }
                field(lang.statusMspt, true) { "```${getTicks(server, false)}```" }
                field(lang.statusCpu, true) { "```${getCpuUsage()}```" }
                field(lang.statusRam, true) { "```${getRamUsage()}```" }
                field("")

                // Player list
                field(Placeholder.replace(lang.statusPlayers, values)) { getPlayerList(lang, server) }
                field("")

                // Last updated footer
                field("") { "-# ${Placeholder.replace(lang.statusUpdate, values)}" }
            }
        }
    }

    // Returns the server's TPS if isTps is true, MSPT otherwise
    private fun getTicks(server: MinecraftServer, isTps: Boolean): String {
        val mspt = server.tickTimes.average() / 1.0E6
        val tps = (1000.0 / mspt).coerceAtMost(20.0)
        return String.format("%.1f", if (isTps) tps else mspt)
    }

    // Returns the server's CPU usage in %
    private fun getCpuUsage(): String {
        val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        val load = osBean.processCpuLoad * 100
        return String.format("%.1f", load) + "%"
    }

    // Returns the server's RAM usage in used MB / max MB
    private fun getRamUsage(): String {
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val max = runtime.maxMemory() / 1024 / 1024
        return "${String.format("%,d", used)} MB / ${String.format("%,d", max)} MB"
    }

    // Returns the server's player count in online / max
    private fun getPlayerCount(server: MinecraftServer): String {
        val current = server.playerManager.currentPlayerCount
        val max = server.playerManager.maxPlayerCount
        return "${String.format("%,d", current)} / ${String.format("%,d", max)}"
    }

    // Returns the server's player list up to 20 players, then counts the remaining
    private fun getPlayerList(lang: LangConfig, server: MinecraftServer): String {
        val players = server.playerManager.playerList
        if (players.isEmpty()) return ">>> ${lang.statusPlayersNone}"
        val max = 20
        val remaining = players.size - max
        val values = mapOf("remaining" to String.format("%,d", remaining))

        return buildString {
            appendLine(">>> ${players.take(max).joinToString("\n") { it.name.string }}")
            if (remaining > 0) append(Placeholder.replace(lang.statusPlayersMore, values))
        }
    }
}