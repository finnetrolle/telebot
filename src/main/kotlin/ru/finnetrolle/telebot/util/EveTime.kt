package ru.finnetrolle.telebot.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */
@Component
open class EveTime {

    @Value("\${evetime.offset}")
    private lateinit var offset: Integer

    fun now(): Date {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.HOUR, offset as Int)
        return cal.time
    }

    fun formatted(): String {
        return "${SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(now())} ET"
    }

}