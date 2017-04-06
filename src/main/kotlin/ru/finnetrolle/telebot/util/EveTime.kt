package ru.finnetrolle.telebot.util

import java.util.*

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */
object EveTime {

    fun now(): Date {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.HOUR, -3)
        return cal.time
    }

}