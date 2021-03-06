package ru.finnetrolle.telebot.service.telegram

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import ru.finnetrolle.telebot.service.internal.PilotService
import ru.finnetrolle.telebot.service.processing.engine.CommandExecutorService
import ru.finnetrolle.telebot.service.telegram.api.BotApi
import ru.finnetrolle.telebot.service.telegram.api.BotApiExtender
import ru.finnetrolle.telebot.service.telegram.api.BotApiStub
import ru.finnetrolle.telebot.service.telegram.broadcasting.BroadcastService
import ru.finnetrolle.telebot.service.telegram.processors.AuthPreprocessor
import ru.finnetrolle.telebot.util.MessageBuilder
import javax.annotation.PostConstruct

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */

@Component
open class TelegramBotService {

    @Autowired private lateinit var authPreprocessor: AuthPreprocessor
    @Autowired private lateinit var broadcastService: BroadcastService
    @Autowired private lateinit var pilotService: PilotService

    @Autowired
    private lateinit var ces: CommandExecutorService

    @Value("\${telegram.bot.token}")
    private lateinit var botToken: String

    @Value("\${telegram.bot.username}")
    private lateinit var botUsername: String

    @Value("\${telegram.bot.alive}")
    private var alive: Boolean = false

    private lateinit var api: BotApi

    @PostConstruct
    fun init() {
        if (alive) {
            log.info("Telegram bot is enabled")
            try {
                api = BotApiExtender(botUsername, botToken, processMessage, pilotService)
                broadcastService.init(api)
            } catch (e: Exception) {
                log.error("Bot cannot be started", e)
            }
        } else {
            log.info("Telegram bot is disabled")
            api = BotApiStub()
        }
    }

    private val processMessage: (Message) -> SendMessage = { msg ->
        try {
            val authResult = authPreprocessor.selectResponse(msg.text, msg.from, msg.chatId.toString())
            when (authResult) {
                is AuthPreprocessor.Auth.Intercepted -> {
                    authResult.response
                }
                is AuthPreprocessor.Auth.Authorized -> {
                    ces.execute(authResult.command, authResult.data, authResult.pilot, authResult.pilot.id.toString())
                }
            }
        } catch (e: Exception) {
            log.error("Some failed", e); MessageBuilder.build(msg.chatId.toString(), "Some bad happend =(")
        }
    }

    open fun broadcast(messages: List<SendMessage>) {
        broadcastService.send(messages)
    }

    companion object {
        private val log = LoggerFactory.getLogger(TelegramBotService::class.java)
    }

}