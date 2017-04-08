package ru.finnetrolle.telebot.util

import java.text.SimpleDateFormat
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

    fun formatted(): String {
        return "${SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(now())} ET"
    }

}