package com.fooze.serverdiscordbot

import com.fooze.serverdiscordbot.config.ConfigHandler
import com.fooze.serverdiscordbot.feature.Announcer
import com.fooze.serverdiscordbot.feature.StatusCommand
import com.fooze.serverdiscordbot.feature.WhitelistCommand
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
	private val logger = LoggerFactory.getLogger("Server Discord Bot")
	var minecraftServer: MinecraftServer? = null
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private var bot: Kord? = null

	override fun onInitializeServer() {
		ConfigHandler.load()
		val config = ConfigHandler.config

		ServerLifecycleEvents.SERVER_STARTED.register { server ->
			minecraftServer = server

			if (config.botToken.isBlank()) {
				logger.warn("Bot token missing! Add it to config/${MOD_ID}.json")
				return@register
			}

			if (config.channelId.isBlank()) {
				logger.warn("Channel ID missing! Add it to config/${MOD_ID}.json")
				return@register
			}


			scope.launch {
				runCatching {
					bot = Kord(config.botToken)

					// Features
					Announcer.load(scope, bot, config, logger)
					StatusCommand.load(scope, bot, config, logger)
					WhitelistCommand.load(scope, bot, config, logger)

                    bot?.on<ReadyEvent> {
                        logger.info("Discord login successful!")
                    }

                    bot?.login()
				}.onFailure {
					if (it !is CancellationException) {
						logger.error("Discord login failed! Your bot token may be invalid", it)
					}
				}
			}
		}

		ServerLifecycleEvents.SERVER_STOPPING.register {
			runBlocking {
				bot?.shutdown()
				bot?.resources?.httpClient?.close()
			}
		}
	}
}