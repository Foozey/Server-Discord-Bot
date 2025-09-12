package com.fooze.serverdiscordbot

import com.fooze.serverdiscordbot.config.ConfigHandler
import com.fooze.serverdiscordbot.feature.Announcer
import com.fooze.serverdiscordbot.feature.StatusCommand
import dev.kord.core.Kord
import kotlinx.coroutines.*
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import org.slf4j.LoggerFactory

object ServerDiscordBot : DedicatedServerModInitializer {
	private val logger = LoggerFactory.getLogger("Server Discord Bot")
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	var minecraftServer: MinecraftServer? = null

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
				StatusCommand.load(scope, bot)
				bot.login()
			}.onFailure {
				logger.error("Discord login failed! Your bot token may be invalid", it)
			}
		}

		ServerLifecycleEvents.SERVER_STARTED.register { server ->
			minecraftServer = server
		}

		ServerLifecycleEvents.SERVER_STOPPING.register {
			minecraftServer = null
			scope.cancel()
		}

		logger.info("Initialized successfully")
	}
}