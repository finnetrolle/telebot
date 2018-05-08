package ru.finnetrolle.telebot.service.external.eve

import feign.Feign
import feign.gson.GsonDecoder
import feign.gson.GsonEncoder
import org.junit.Test

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */
class EveEsiApiTest {

    companion object {
        val FIRST = 95538921L
        val SECOND = 95538922L
    }

    @Test
    fun checkAffiliationRequest() {
        val api: EveEsiApi = Feign.builder().encoder(GsonEncoder()).decoder(GsonDecoder()).target(EveEsiApi::class.java, "https://esi.evetech.net")
        val affs = api.getAffiliation(setOf(FIRST,SECOND))
        assert(affs.find { it.character_id == FIRST }?.alliance_id == 434243723L)
        assert(affs.find { it.character_id == FIRST }?.corporation_id == 109299958L)
        assert(affs.find { it.character_id == SECOND }?.corporation_id == 1000014L)
        assert(affs.find { it.character_id == SECOND }?.alliance_id == 0L)
    }
}