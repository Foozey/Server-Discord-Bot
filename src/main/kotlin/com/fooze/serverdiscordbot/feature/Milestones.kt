package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.feature.Announcer.announcePlayerEvent
import com.fooze.serverdiscordbot.util.Colors
import com.fooze.serverdiscordbot.util.Format
import com.fooze.serverdiscordbot.util.PlayerStats
import dev.kord.core.Kord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap

object Milestones {
    // Stores the completed milestones and initialized players
    private val completedMilestones = ConcurrentHashMap<String, MutableMap<String, Int>>()
    private val initializedPlayers = ConcurrentHashMap<String, Boolean>()

    fun load(scope: CoroutineScope, bot: Kord?, config: ModConfig, lang: LangConfig, logger: Logger) {
        ServerTickEvents.END_SERVER_TICK.register { server ->
            for (player in server.playerManager.playerList) {
                val uuid = player.uuidAsString
                val name = player.name.string
                val milestones = getMilestones(config, lang, server, name)

                // Get or create a map for the player's milestones
                val playerMilestones = completedMilestones.computeIfAbsent(uuid) { ConcurrentHashMap() }

                // If the player isn't initialized (e.g., first join on restart), store the milestones already reached
                if (initializedPlayers.putIfAbsent(uuid, true) == null) {
                    for ((stat, interval, _, key) in milestones) {
                        playerMilestones[key] = stat / interval
                    }
                } else {
                    scope.launch {
                        for ((stat, interval, message, key) in milestones) {
                            val last = playerMilestones.getOrDefault(key, 0)
                            val current = stat / interval

                            // If the player's current milestone > last stored milestone, store the current milestone
                            if (current > last) {
                                playerMilestones[key] = current

                                // Calculate the milestone value to announce
                                val milestoneValue = current * interval

                                // Placeholders
                                val placeholders = mapOf(
                                    "player" to Format.escape(name),
                                    "milestone" to Format.number(milestoneValue)
                                )

                                // Send milestone announcement
                                announcePlayerEvent(
                                    bot = bot,
                                    config = config,
                                    lang = lang,
                                    logger = logger,
                                    description = null,
                                    color = Colors.BLUE,
                                    message = Format.replace(message, placeholders),
                                    player = name
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Returns a list of milestones for the given player
    private fun getMilestones(config: ModConfig, lang: LangConfig, server: MinecraftServer, name: String): List<Milestone> {
        val player = server.playerManager.getPlayer(name)

        if (player != null) {
            val stats = PlayerStats.get(server, player.gameProfile)

            return listOf(
                Milestone(stats.deaths, config.milestoneDeaths, lang.milestoneDeaths, "deaths"),
                Milestone(stats.playerKills, config.milestonePlayerKills, lang.milestonePlayerKills, "playerKills"),
                Milestone(stats.mobKills, config.milestoneMobKills, lang.milestoneMobKills, "mobKills"),
                Milestone(stats.blocksMined, config.milestoneBlocksMined, lang.milestoneBlocksMined, "blocksMined"),
                Milestone(stats.blocksPlaced, config.milestoneBlocksPlaced, lang.milestoneBlocksPlaced, "blocksPlaced"),
                Milestone(stats.itemsCrafted, config.milestoneItemsCrafted, lang.milestoneItemsCrafted, "itemsCrafted"),
                Milestone(stats.timePlayed / 72000, config.milestoneTimePlayed, lang.milestoneTimePlayed, "timePlayed")
            ).filter { it.interval > 0 }
        } else {
            return emptyList()
        }
    }

    // Defines a milestone for a stat
    private data class Milestone(
        val stat: Int, // The stat to track
        val interval: Int, // The interval per milestone (e.g., every 10 deaths)
        val message: String, // The message to send
        val key: String // The key for storing the milestone in a map
    )
}