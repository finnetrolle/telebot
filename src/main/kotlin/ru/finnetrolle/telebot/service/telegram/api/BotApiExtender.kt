package ru.finnetrolle.telebot.service.telegram.api

import org.slf4j.LoggerFactory
import org.telegram.telegrambots.TelegramApiException
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import ru.finnetrolle.telebot.service.internal.PilotService
import ru.finnetrolle.telebot.util.MessageBuilder
import ru.finnetrolle.telebot.util.decide

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */

open class BotApiExtender(
        val name: String,
        val token: String,
        val processIncomingMessage: (Message) -> SendMessage,
        val pilotService: PilotService
) : BotApi {

    private var api = object : TelegramLongPollingBot() {
        override fun getBotUsername() = name

        override fun getBotToken() = token

        override fun onUpdateReceived(request: Update) {
            when {
                request.hasMessage() -> {
                    log.trace("Received request from chatId = ${request.message?.chatId} with text = ${request.message?.text}")
                    send(processIncomingMessage.invoke(request.message))
                }
                request.hasChosenInlineQuery() -> {
                    log.warn("Not supported format - ChosenInlineQuery from ${request.chosenInlineQuery.from.id}")
                }
                request.hasInlineQuery() -> {
                    log.warn("Not supported format - InlineQuery from ${request.inlineQuery.from.id}")
                }
                else -> {
                    log.warn("Impossible Exception for unknown request type")
                }
            }
        }
    }

    init {
        try {
            TelegramBotsApi().registerBot(api)
        } catch (e: TelegramApiException) {
            log.error("Can't connect to telegram", e)
            log.error("Response is ${e.message}")
            log.error("Exit")
            System.exit(1)
        }
    }

    private val BLOCKED_BOT_MESSAGE: String = "Forbidden: bot was blocked by the user"
    private val DEACTIVATED_BOT_MESSAGE: String = "Forbidden: user is deactivated"


    override fun send(message: SendMessage): BotApi.Send {
        try {
            val messages = MessageBuilder.split(message)
            val start = System.currentTimeMillis()
            if (messages.size > 1) {
                messages.forEach { m -> send(m) }
                return BotApi.Send.Success(message.chatId.toLong(), System.currentTimeMillis() - start)
            } else {
                log.trace("Trying to send response to ${message.chatId}")
                return BotApi.Send.Success(
                        api.sendMessage(message).chatId,
                        System.currentTimeMillis() - start)
            }
        } catch (e: TelegramApiException) {
            if (e.apiResponse.equals(BLOCKED_BOT_MESSAGE) || e.apiResponse.equals(DEACTIVATED_BOT_MESSAGE)) {
                log.debug("Pilot ${message.chatId} must be removed")
                pilotService.remove(message.chatId.toInt()).decide({
                    log.debug("REMOVED: ${it.characterName} [${it.characterId} because of ${e.apiResponse}]")
                },{
                    log.warn("Can't remove user with id ${message.chatId} because of db")
                })
            } else {
                log.error("Message sent is failed. API Response = [${e.apiResponse}], message = [${e.message}]", e)
            }
            return BotApi.Send.Failed(message.chatId.toLong(), e)
        } catch (e: Exception) {
            log.error("Message sending failed because of", e)
            return BotApi.Send.Failed(message.chatId.toLong(), TelegramApiException(e.message))
        }
    }

    companion object {
        val log = LoggerFactory.getLogger(BotApiExtender::class.java)
    }

}