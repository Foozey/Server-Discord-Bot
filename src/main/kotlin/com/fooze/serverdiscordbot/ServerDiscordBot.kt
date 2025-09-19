package com.fooze.serverdiscordbot

import com.fooze.serverdiscordbot.config.ConfigHandler
import com.fooze.serverdiscordbot.feature.*
import com.fooze.serverdiscordbot.util.Colors
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

	override fun onInitializeServer() {
		ConfigHandler.load()
		val config = ConfigHandler.config
        val lang = ConfigHandler.lang

        // On server start
		ServerLifecycleEvents.SERVER_STARTED.register { server ->
			this.server = server

			if (config.discordBotToken.isBlank()) {
				logger.warn(lang.logBotTokenMissing)
				return@register
			}

			if (config.discordChannelId.isBlank()) {
				logger.warn(lang.logChannelIdMissing)
				return@register
			}

			scope.launch {
				runCatching {
					bot = Kord(config.discordBotToken)

                    // Send server start announcement
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
					Announcer.load(scope, bot, config, lang, logger)
					StatusCommand.load(bot, config, lang, logger, server)
					WhitelistCommand.load(bot, config, lang, logger, server)
                    StatsCommand.load(bot, config, lang, logger, server)
                    HelpCommand.load(bot, config, lang, logger)

                    // Start the bot
                    bot?.on<ReadyEvent> { logger.info(lang.logLoginSuccess) }
                    bot?.login()
				}.onFailure {
                    logger.error(lang.logLoginFail, it)
				}
			}
		}

        // On server stop
		ServerLifecycleEvents.SERVER_STOPPING.register {
			runBlocking {
                // Send server stop announcement
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