package com.fooze.serverdiscordbot

import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object ServerDiscordBot : DedicatedServerModInitializer {
    private val logger = LoggerFactory.getLogger("Server Discord Bot")

	override fun onInitializeServer() {
		logger.info("Initialized successfully")
	}
}