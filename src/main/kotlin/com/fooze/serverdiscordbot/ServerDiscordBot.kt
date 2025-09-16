package com.fooze.serverdiscordbot

import com.fooze.serverdiscordbot.config.ConfigHandler
import com.fooze.serverdiscordbot.feature.Announcer
import com.fooze.serverdiscordbot.feature.Announcer.announceServerEvent
import com.fooze.serverdiscordbot.feature.StatusCommand
import com.fooze.serverdiscordbot.feature.WhitelistCommand
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
	lateinit var server: MinecraftServer
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private lateinit var bot: Kord

	override fun onInitializeServer() {
		ConfigHandler.load()
		val config = ConfigHandler.config

		ServerLifecycleEvents.SERVER_STARTED.register { server ->
			this.server = server

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
                    announceServerEvent(bot, config, logger, "started", "online — connect using `${config.serverIp}`", Colors.GREEN)

					// Features
					Announcer.load(scope, bot, config, logger)
					StatusCommand.load(bot, config, logger)
					WhitelistCommand.load(bot, config, logger)

                    bot.on<ReadyEvent> {
                        logger.info("Discord login successful!")
                    }

                    bot.login()
				}.onFailure {
                    logger.error("Discord login failed! Your bot token may be invalid", it)
				}
			}
		}

		ServerLifecycleEvents.SERVER_STOPPING.register {
			runBlocking {
                announceServerEvent(bot, config, logger, "stopped", "offline — see you soon!", Colors.RED)
				bot.shutdown()
                bot.resources.httpClient.close()
			}
		}
	}
}