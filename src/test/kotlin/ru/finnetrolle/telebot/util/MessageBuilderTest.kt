package ru.finnetrolle.telebot.util

import org.junit.Test

import org.junit.Assert.*

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */
class MessageBuilderTest {

    private val CHAT_ID = "chatid"
    private val TEXT = "some text"

    @Test
    fun checkMessageBuilding() {
        val msg = MessageBuilder.build(CHAT_ID, TEXT)
        assertEquals(CHAT_ID, msg.chatId)
        assertEquals(TEXT, msg.text)
    }

    @Test
    fun checkSplitting() {
        val s = StringBuilder()
        (1..500).forEach { i -> s.append("Hello world! I'm Mike!\n") }
        assertEquals(11500, s.toString().length)
        val message = MessageBuilder.build("12345", s.toString())
        assertEquals(4, MessageBuilder.split(message).size)
    }

}