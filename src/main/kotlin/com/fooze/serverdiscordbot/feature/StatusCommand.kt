package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.ServerDiscordBot
import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.util.Placeholder
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

    private fun getPlayerList(server: MinecraftServer, lang: LangConfig): String {
        val players = server.playerManager.playerList
        if (players.isEmpty()) return ">>> ${lang.statusPlayersNone}"
        val maxSize = 10
        val remaining = players.size - maxSize
        val values = mapOf("remaining" to remaining.toString())

        return buildString {
            appendLine(">>> ${players.take(maxSize).joinToString("\n") { it.name.string }}")
            if (remaining > 0) append(Placeholder.replace(lang.statusPlayersMore, values))
        }
    }

    suspend fun load(bot: Kord, config: ModConfig, lang: LangConfig, logger: Logger) {
        // Create the command
        val statusCommand = runCatching {
            val channel = bot.getChannelOf<TextChannel>(Snowflake(config.channelId)) ?: return
            bot.createGuildChatInputCommand(channel.guildId, lang.statusCommand, lang.statusCommandInfo)
        }.onFailure {
            logger.error(lang.logStatusFail, it)
        }.getOrNull() ?: return

        // Create the interaction
        bot.on<GuildChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != statusCommand.name) return@on
            val server = ServerDiscordBot.server

            // Placeholders
            val values = mapOf(
                "count" to getPlayerCount(server),
                "time" to Instant.now().epochSecond.toString(),
                "id" to statusCommand.id.toString()
            )

            // Embed formatting
            interaction.deferPublicResponse().respond {
                embed {
                    title = lang.statusTitle
                    field(lang.statusState, true) { "```\uD83D\uDFE2 ${lang.statusStateValue}```" }
                    field(lang.statusTps, true) { "```${getTicks(server, true)}```" }
                    field(lang.statusMspt, true) { "```${getTicks(server, false)}```" }
                    field(lang.statusCpu, true) { "```${getCpuUsage()}```" }
                    field(lang.statusRam, true) { "```${getRamUsage()}```" }
                    field("")
                    field(Placeholder.replace(lang.statusPlayers, values)) { getPlayerList(server, lang) }
                    field("")
                    field("") { "-# ${Placeholder.replace(lang.statusUpdate, values)}" }
                }
            }
        }
    }
}