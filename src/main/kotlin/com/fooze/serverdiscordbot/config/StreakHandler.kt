package com.fooze.serverdiscordbot.config

import com.fooze.serverdiscordbot.ServerDiscordBot.MOD_ID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object StreakHandler {
    private val file = File("config/${MOD_ID}/streaks.json")
    private val data = mutableMapOf<String, StreakData>()

    // JSON serializer
    private val json = Json {
        prettyPrint = true
    }

    // Loads the streak data from the file
    fun load() {
        file.parentFile.mkdirs()

        if (file.exists()) {
            data.putAll(json.decodeFromString(file.readText()))
        }
    }

    // Gets the streak of the provided player
    fun getStreak(player: String): Streak {
        val today = LocalDate.now()

        // If the player has no streak data, create a new streak data object
        val streakData = data.getOrPut(player) {
            StreakData(today.toString(), 1).also { save() }
        }

        val lastJoin = LocalDate.parse(streakData.lastJoin)
        val daysSince = ChronoUnit.DAYS.between(lastJoin, today)

        // Calculate the streak
        val count = when (daysSince) {
            0L -> streakData.streak // Player already joined today
            1L -> streakData.streak + 1 // Player joined consecutive days
            else -> 1 // Player missed a day
        }

        // Update the streak data if the player joined a new day
        val updated = daysSince != 0L

        if (updated) {
            streakData.lastJoin = today.toString()
            streakData.streak = count
            save()
        }

        return Streak(count, updated)
    }

    // Saves the streak data to the file
    private fun save() {
        file.writeText(json.encodeToString(data))
    }

    data class Streak(
        val count: Int, // The current streak of the player
        val updated: Boolean // Whether the streak has been updated
    )

    @Serializable
    private data class StreakData(
        var lastJoin: String, // The last date the player joined the server
        var streak: Int // The current streak of the player
    )
}