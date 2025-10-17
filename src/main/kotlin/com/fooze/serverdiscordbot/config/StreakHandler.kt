package com.fooze.serverdiscordbot.config

import com.fooze.serverdiscordbot.ServerDiscordBot.MOD_ID
import com.fooze.serverdiscordbot.util.Format
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object StreakHandler {
    private val file = File("config/${MOD_ID}/streaks.json")
    private val data = mutableMapOf<String, StreakData>()
    private var isValid = true

    // JSON serializer
    private val json = Json {
        prettyPrint = true
    }

    // Loads the streak data from the file if it exists
    fun load(logger: Logger, lang: LangConfig) {
        if (!file.exists()) return

        runCatching {
            data.putAll(json.decodeFromString(file.readText()))
        }.onFailure {
            // Placeholders
            val placeholders = mapOf("path" to file.path)

            logger.error(Format.replace(lang.logStreakDataInvalid, placeholders))
            logger.error(it.message)
            isValid = false
        }
    }

    // Gets the streak of the given player
    fun getStreak(logger: Logger, lang: LangConfig, player: String): Streak {
        // If the streak data is invalid, return a zero streak
        if (!isValid) {
            return Streak(0, false)
        }

        // Get the current date
        val today = LocalDate.now()

        // If the player has no streak data, create a new streak data object
        val streakData = data.getOrPut(player) {
            StreakData(today.toString(), 1).also {
                save(lang, logger)
            }
        }

        // Get the last join date of the player or set to today if invalid
        val lastJoin = runCatching {
            LocalDate.parse(streakData.lastJoin)
        }.getOrElse {
            streakData.lastJoin = today.toString()
            save(lang, logger)
            today
        }

        // If the streak is 0 or below, reset it to 1
        if (streakData.streak <= 0) {
            streakData.streak = 1
            save(lang, logger)
        }

        // Get the days since the last join date
        val daysSince = ChronoUnit.DAYS.between(lastJoin, today)
        val updated = daysSince != 0L

        // Calculate the streak
        val count = when (daysSince) {
            0L -> streakData.streak // Player already joined today
            1L -> streakData.streak + 1 // Player joined consecutive days
            else -> 1 // Player missed a day
        }

        // Update the streak data if the player joined a new day
        if (updated) {
            streakData.lastJoin = today.toString()
            streakData.streak = count
            save(lang, logger)
        }

        return Streak(count, updated)
    }

    // Saves the streak data to the file
    private fun save(lang: LangConfig, logger: Logger) {
        runCatching {
            file.parentFile.mkdirs()
            file.writeText(json.encodeToString(data))
        }.onFailure {
            // Placeholders
            val placeholders = mapOf("path" to file.path)

            logger.error(Format.replace(lang.logStreakDataSaveFail, placeholders))
            logger.error(it.message)
        }
    }

    data class Streak(
        val count: Int, // The current streak of the player
        val updated: Boolean // Whether the streak has been updated today
    )

    @Serializable
    private data class StreakData(
        var lastJoin: String, // The last date the player joined the server
        var streak: Int // The current streak of the player
    )
}