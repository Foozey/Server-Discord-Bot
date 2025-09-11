package com.fooze.serverdiscordbot

import com.fooze.serverdiscordbot.config.ConfigHandler
import net.fabricmc.api.DedicatedServerModInitializer
import org.slf4j.LoggerFactory

object ServerDiscordBot : DedicatedServerModInitializer {
	const val MOD_ID = "server-discord-bot"
    private val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitializeServer() {
		logger.info("Initialized successfully")
		ConfigHandler.load()
	}
}