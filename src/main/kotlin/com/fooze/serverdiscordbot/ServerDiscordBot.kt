package com.fooze.serverdiscordbot

import com.fooze.serverdiscordbot.config.ConfigHandler
import dev.kord.core.Kord
import kotlinx.coroutines.*
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.slf4j.LoggerFactory

object ServerDiscordBot : DedicatedServerModInitializer {
	const val MOD_ID = "server-discord-bot"
	private val logger = LoggerFactory.getLogger(MOD_ID)
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName(MOD_ID))
	private var kord: Kord? = null

	override fun onInitializeServer() {
		ConfigHandler.load()
		val config = ConfigHandler.config

		if (config.botToken.isBlank()) {
			logger.warn("Bot token missing! Add it to config/server-discord-bot.json")
			return
		}

		if (config.channelId.isBlank()) {
			logger.warn("Channel ID missing! Add it to config/server-discord-bot.json")
			return
		}

		scope.launch {
			runCatching {
				val bot = Kord(config.botToken)
				kord = bot
				bot.login()
			}.onFailure { throwable ->
				logger.error("Failed to login to Discord", throwable)
			}
		}

		ServerLifecycleEvents.SERVER_STOPPING.register {
			scope.cancel()
			kord = null
		}

		logger.info("Initialized successfully")

		// TODO: Load features here
	}
}