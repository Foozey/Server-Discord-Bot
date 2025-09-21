package com.fooze.serverdiscordbot.feature

import com.fooze.serverdiscordbot.config.LangConfig
import com.fooze.serverdiscordbot.config.ModConfig
import com.fooze.serverdiscordbot.feature.Announcer.announcePlayerEvent
import com.fooze.serverdiscordbot.util.Colors
import com.fooze.serverdiscordbot.util.Placeholder
import dev.kord.core.Kord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.registry.Registries
import net.minecraft.stat.ServerStatHandler
import net.minecraft.stat.Stats
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap

object Milestones {
    // Stores the completed milestones and initialized players
    private val completedMilestones = ConcurrentHashMap<String, MutableMap<String, Int>>()
    private val initializedPlayers = ConcurrentHashMap<String, Boolean>()

    fun load(scope: CoroutineScope, bot: Kord?, config: ModConfig, lang: LangConfig, logger: Logger) {
        ServerTickEvents.END_SERVER_TICK.register { server ->
            for (player in server.playerManager.playerList) {
                val key = player.uuidAsString
                val stats = player.statHandler
                val name = player.name.string
                val milestones = getMilestones(stats, lang)

                // Get or create a map for the player's milestones
                val playerMilestones = completedMilestones.computeIfAbsent(key) { ConcurrentHashMap() }

                // If the player isn't initialized (e.g., first join on restart), store the milestones already reached
                if (initializedPlayers.putIfAbsent(key, true) == null) {
                    for ((stat, interval, _, key) in milestones) {
                        playerMilestones[key] = stat / interval
                    }
                } else {
                    scope.launch {
                        for ((stat, interval, message, key) in milestones) {
                            val last = playerMilestones[key] ?: 0
                            val current = stat / interval

                            // If the player's current milestone > last stored milestone, store the current milestone
                            if (current > last) {
                                playerMilestones[key] = current

                                // Calculate the milestone value to announce
                                val milestoneValue = current * interval

                                // Placeholders
                                val values = mapOf(
                                    "player" to name,
                                    "count" to milestoneValue.toString()
                                )

                                // Send milestone announcement
                                announcePlayerEvent(
                                    bot = bot,
                                    config = config,
                                    lang = lang,
                                    logger = logger,
                                    description = null,
                                    color = Colors.BLUE,
                                    message = Placeholder.replace(message, values),
                                    player = name
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getMilestones(stats: ServerStatHandler, lang: LangConfig) = listOf(
        // Deaths
        Milestone(
            stat = StatsCommand.getStat(stats, Stats.DEATHS),
            interval = 10,
            message = lang.milestoneDeaths,
            key = "deaths"
        ),

        // Player Kills
        Milestone(
            stat = StatsCommand.getStat(stats, Stats.PLAYER_KILLS),
            interval = 10,
            message = lang.milestonePlayerKills,
            key = "playerKills"
        ),

        // Mob Kills
        Milestone(
            stat = StatsCommand.getStat(stats, Stats.MOB_KILLS),
            interval = 1000,
            message = lang.milestoneMobKills,
            key = "mobKills"
        ),

        // Blocks Mined
        Milestone(
            stat = StatsCommand.getTotal(stats, Registries.BLOCK, Stats.MINED) { it },
            interval = 10000,
            message = lang.milestoneBlocksMined,
            key = "blocksMined"
        ),

        // Blocks Placed
        Milestone(
            stat = StatsCommand.getTotal(stats, Registries.BLOCK, Stats.USED) { it.asItem() },
            interval = 10000,
            message = lang.milestoneBlocksPlaced,
            key = "blocksPlaced"
        ),

        // Items Crafted
        Milestone(
            stat = StatsCommand.getTotal(stats, Registries.ITEM, Stats.CRAFTED) { it },
            interval = 10000,
            message = lang.milestoneItemsCrafted,
            key = "itemsCrafted"
        ),

        // Time Played
        Milestone(
            stat = StatsCommand.getStat(stats, Stats.PLAY_TIME) / 72000,
            interval = 100,
            message = lang.milestoneTimePlayed,
            key = "timePlayed"
        )
    )

    private data class Milestone(
        val stat: Int, // The stat to track
        val interval: Int, // The interval per milestone (e.g., every 10 deaths)
        val message: String, // The message to send
        val key: String // The key for storing the milestone in a map
    )
}