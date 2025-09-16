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
        val lang = ConfigHandler.lang

        // On server start
		ServerLifecycleEvents.SERVER_STARTED.register { server ->
			this.server = server

			if (config.botToken.isBlank()) {
				logger.warn(lang.logBotTokenMissing)
				return@register
			}

			if (config.channelId.isBlank()) {
				logger.warn(lang.logChannelIdMissing)
				return@register
			}

            // Load the bot
			scope.launch {
				runCatching {
					bot = Kord(config.botToken)

                    announceServerEvent(
                        bot = bot,
                        config = config,
                        lang = lang,
                        logger = logger,
                        title = lang.announceStart,
                        description = lang.announceStartDescription,
                        color = Colors.GREEN
                    )

					// Features
					Announcer.load(scope, bot, config, lang, logger)
					StatusCommand.load(bot, config, lang, logger)
					WhitelistCommand.load(bot, config, lang, logger)

                    bot.on<ReadyEvent> {
                        logger.info(lang.logLoginSuccess)
                    }

                    bot.login()
				}.onFailure {
                    logger.error(lang.logLoginFail, it)
				}
			}
		}

        // On server stop
		ServerLifecycleEvents.SERVER_STOPPING.register {
			runBlocking {
                announceServerEvent(
                    bot = bot,
                    config = config,
                    lang = lang,
                    logger = logger,
                    title = lang.announceStop,
                    description = lang.announceStopDescription,
                    color = Colors.RED
                )

                // Shutdown the bot
                bot.shutdown()
                bot.resources.httpClient.close()
			}
		}
	}
}