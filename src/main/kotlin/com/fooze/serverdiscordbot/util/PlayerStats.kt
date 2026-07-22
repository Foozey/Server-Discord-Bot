package com.fooze.serverdiscordbot.util

import com.mojang.authlib.GameProfile
import net.minecraft.core.DefaultedRegistry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.stats.ServerStatsCounter
import net.minecraft.stats.StatType
import net.minecraft.stats.Stats

object PlayerStats {
    // Returns a player's stats
    fun get(server: MinecraftServer, profile: GameProfile): PlayerStats {
        val player = server.playerList.getPlayer(profile.name)

        // If the player is online, use their server stats, otherwise use the stat file
        val statCounter = if (player != null) {
            player.stats
        } else {
            val world = server.worldData.levelName
            val file = server.serverDirectory.resolve("${world}/players/stats/${profile.id}.json").toFile()
            ServerStatsCounter(server, file.toPath())
        }

        return PlayerStats(
            deaths = getStat(statCounter, Stats.DEATHS),
            playerKills = getStat(statCounter, Stats.PLAYER_KILLS),
            mobKills = getStat(statCounter, Stats.MOB_KILLS),
            blocksMined = getTotal(statCounter, BuiltInRegistries.BLOCK, Stats.BLOCK_MINED) { it },
            blocksPlaced = getTotal(statCounter, BuiltInRegistries.BLOCK, Stats.ITEM_USED) { it.asItem() },
            itemsCrafted = getTotal(statCounter, BuiltInRegistries.ITEM, Stats.ITEM_CRAFTED) { it },
            timePlayed = getStat(statCounter, Stats.PLAY_TIME)
        )
    }

    // Defines a player's stats
    data class PlayerStats(
        val deaths: Int,
        val playerKills: Int,
        val mobKills: Int,
        val blocksMined: Int,
        val blocksPlaced: Int,
        val itemsCrafted: Int,
        val timePlayed: Int,
    )

    // Returns the value of the given stat
    private fun getStat(statHandler: ServerStatsCounter, stat: Identifier): Int {
        return statHandler.getValue(Stats.CUSTOM.get(stat))
    }

    // Returns the total of the given stats
    private fun <Entry : Any, Stat : Any> getTotal(
        statCounter: ServerStatsCounter,
        registry: DefaultedRegistry<Entry>,
        statType: StatType<Stat>,
        map: (Entry) -> Stat?
    ): Int {
        return registry.sumOf { entry ->
            val stat = map(entry)

            if (stat != null) {
                statCounter.getValue(statType.get(stat))
            } else {
                0
            }
        }
    }
}