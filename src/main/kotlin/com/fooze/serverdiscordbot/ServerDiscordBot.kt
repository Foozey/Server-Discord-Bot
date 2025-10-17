package com.fooze.serverdiscordbot

import com.fooze.serverdiscordbot.config.ConfigHandler
import com.fooze.serverdiscordbot.config.StreakHandler
import com.fooze.serverdiscordbot.feature.Announcer
import com.fooze.serverdiscordbot.feature.Milestones
import com.fooze.serverdiscordbot.feature.commands.*
import com.fooze.serverdiscordbot.util.Colors
import com.fooze.serverdiscordbot.util.Format
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import kotlinx.coroutines.*
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import org.slf4j.LoggerFactory

object ServerDiscordBot : DedicatedServerModInitializer {
	const val MOD_ID = "server-discord-bot"
	private val logger = LoggerFactory.getLogger(MOD_ID)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private var bot: Kord? = null
    private var server: MinecraftServer? = null
    var stopping = false

	override fun onInitializeServer() {
        // Load configs
		ConfigHandler.load(logger)
        val config = ConfigHandler.config
        val lang = ConfigHandler.lang

        // Load streak data
        StreakHandler.load(logger, lang)

        // On server start
		ServerLifecycleEvents.SERVER_STARTED.register { server ->
			this.server = server

            // Placeholders
            val placeholders = mapOf("modid" to MOD_ID)

            // Check if bot token and channel ID are set
			if (config.discordBotToken.isBlank()) {
				logger.warn(Format.replace(lang.logBotTokenMissing, placeholders))
				return@register
			}

			if (config.discordChannelId.isBlank()) {
				logger.warn(Format.replace(lang.logChannelIdMissing, placeholders))
				return@register
			}

			scope.launch {
				runCatching {
					bot = Kord(config.discordBotToken)

                    // Send start announcement
                    Announcer.announceServerEvent(
                        bot = bot,
                        config = config,
                        lang = lang,
                        logger = logger,
                        title = lang.announceStartTitle,
                        description = lang.announceStartDescription,
                        color = Colors.GREEN
                    )

					// Load features
					Announcer.load(logger, scope, bot, config, lang)
					StatusCommand.load(logger, bot, server, config, lang)
					WhitelistCommand.load(logger, bot, server, config, lang)
                    StatsCommand.load(logger, bot, server, config, lang)
                    LeaderboardCommand.load(logger, bot, server, config, lang)
                    HelpCommand.load(logger, bot, null, config, lang)
                    Milestones.load(logger, scope, bot, config, lang)

                    // Start the bot
                    bot?.on<ReadyEvent> {
                        logger.info(lang.logLoginSuccess)
                    }

                    bot?.login()
				}.onFailure {
                    logger.error(lang.logLoginFail)
				}
			}
		}

        // On server stop
		ServerLifecycleEvents.SERVER_STOPPING.register {
            stopping = true

			runBlocking {
                // Send stop announcement
                Announcer.announceServerEvent(
                    bot = bot,
                    config = config,
                    lang = lang,
                    logger = logger,
                    title = lang.announceStopTitle,
                    description = lang.announceStopDescription,
                    color = Colors.RED
                )

                // Stop the bot
                bot?.shutdown()
                bot?.resources?.httpClient?.close()
			}
		}
	}
}