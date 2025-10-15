package com.fooze.serverdiscordbot.util

import com.mojang.authlib.GameProfile
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.stat.ServerStatHandler
import net.minecraft.stat.StatType
import net.minecraft.stat.Stats
import net.minecraft.util.Identifier

object PlayerStats {
    // Returns a player's stats
    fun get(server: MinecraftServer, profile: GameProfile): PlayerStats {
        val player = server.playerManager.getPlayer(profile.name)

        // If the player is online, use their server stats, otherwise use the stat file
        val statHandler = if (player != null) {
            player.statHandler
        } else {
            val world = server.saveProperties.levelName
            val file = server.runDirectory.resolve("${world}/stats/${profile.id}.json").toFile()
            ServerStatHandler(server, file)
        }

        return PlayerStats(
            deaths = getStat(statHandler, Stats.DEATHS),
            playerKills = getStat(statHandler, Stats.PLAYER_KILLS),
            mobKills = getStat(statHandler, Stats.MOB_KILLS),
            blocksMined = getTotal(statHandler, Registries.BLOCK, Stats.MINED) { it },
            blocksPlaced = getTotal(statHandler, Registries.BLOCK, Stats.USED) { it.asItem() },
            itemsCrafted = getTotal(statHandler, Registries.ITEM, Stats.CRAFTED) { it },
            timePlayed = getStat(statHandler, Stats.PLAY_TIME)
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
    private fun getStat(statHandler: ServerStatHandler, stat: Identifier): Int {
        return statHandler.getStat(Stats.CUSTOM.getOrCreateStat(stat))
    }

    // Returns the total of the given stats
    private fun <Entry, Stat> getTotal(
        statHandler: ServerStatHandler,
        registry: DefaultedRegistry<Entry>,
        statType: StatType<Stat>,
        map: (Entry) -> Stat?
    ): Int {
        return registry.sumOf { entry ->
            val stat = map(entry)

            if (stat != null) {
                statHandler.getStat(statType.getOrCreateStat(stat))
            } else {
                0
            }
        }
    }
}