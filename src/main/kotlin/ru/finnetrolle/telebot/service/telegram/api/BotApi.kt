package ru.finnetrolle.telebot.service.telegram.api

import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.exceptions.TelegramApiException

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */
interface BotApi {

    sealed class Send () {
        class Success(val chatId: Long, val spend: Long) : Send()
        class Failed(val chatId: Long, val e: TelegramApiException) : Send()
    }

    fun send(message: SendMessage): Send

}