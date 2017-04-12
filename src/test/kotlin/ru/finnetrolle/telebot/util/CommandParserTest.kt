package ru.finnetrolle.telebot.util

import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Created by finnetrolle on 08.03.2017.
 */
class CommandParserTest {

    @Test
    fun successParseFlow() {
        val text = "capitals 60 [one|two|more] How many ships do you have?"
        val parsed = CommandParser.parseQuestData(text)
        assertEquals("capitals", parsed.groupName)
        assertEquals(60L, parsed.mins)
        assertEquals("How many ships do you have?", parsed.text)
        assertArrayEquals(Arrays.asList("one", "two", "more").toTypedArray(), parsed.options.toTypedArray())
    }

    @Test
    fun correctAutoGroupName() {
        assertEquals("ALL", CommandParser.parseQuestData("60 [a|b] so?").groupName)
    }

    @Test
    fun correctAutoTime() {
        assertEquals(1440L, CommandParser.parseQuestData("[a|b] so?").mins)
    }

    @Test
    fun karerTest() {
        val parsed = CommandParser.parseQuestData("capitals 60 [Я застрял, нужен вывод (I'm stuck and need help)|Меня вывели, все норм (I left tower and all fine)] Вынимание, просьба ответить всех кто еще не вывел капитал из под поса в Венале.   All who still stuck under tower in Venal, please, vote.")
        assertEquals(2, parsed.options.size)
        assertEquals("capitals", parsed.groupName)
        assertEquals(60L, parsed.mins)
    }


}