package com.fooze.serverdiscordbot

import com.fooze.serverdiscordbot.config.ConfigHandler
import com.fooze.serverdiscordbot.feature.Announcer
import dev.kord.core.Kord
import kotlinx.coroutines.*
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.slf4j.LoggerFactory

object ServerDiscordBot : DedicatedServerModInitializer {
	const val MOD_ID = "server-discord-bot"
	private val logger = LoggerFactory.getLogger(MOD_ID)
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	override fun onInitializeServer() {
		ConfigHandler.load()
		val config = ConfigHandler.config

		when {
			config.botToken.isBlank() -> {
				logger.warn("Bot token missing! Add it to config/server-discord-bot.json")
				return
			}

			config.channelId.isBlank() -> {
				logger.warn("Channel ID missing! Add it to config/server-discord-bot.json")
				return
			}
		}

		scope.launch {
			runCatching {
				val bot = Kord(config.botToken)
				Announcer.load(scope, bot, config, logger)
				bot.login()
			}.onFailure {
				logger.error("Failed to login to Discord", it)
			}
		}

		ServerLifecycleEvents.SERVER_STOPPING.register {
			scope.cancel()
		}

		logger.info("Initialized successfully")
	}
}