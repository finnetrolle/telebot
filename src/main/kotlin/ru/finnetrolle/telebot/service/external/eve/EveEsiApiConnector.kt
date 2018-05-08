package ru.finnetrolle.telebot.service.external.eve

import feign.Feign
import feign.FeignException
import feign.gson.GsonDecoder
import feign.gson.GsonEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.finnetrolle.telebot.util.getPage
import ru.finnetrolle.telebot.util.getPagesCount
import javax.annotation.PostConstruct

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */

@Component
class EveEsiApiConnector{

    fun getAffiliations(pilotIds: Set<Long>): Map<Long, EveEsiApi.Affiliation> {
        return try {
            val list = pilotIds.toList()
            (0..list.getPagesCount(100) - 1)
                    .map { list.getPage(it, 100) }
                    .flatMap { api.getAffiliation(it.toSet()) }
                    .map { it.character_id to it }
                    .toMap()
        } catch (e: FeignException) {
            emptyMap() //TODO FIX THIS - ITS BAD
        }
    }

    fun getAlliance(id: Long): EveEsiApi.Alliance? {
        return try {
            api.getAlliance(id)
        } catch (e: FeignException) {
            null
        }
    }

    fun getCorporation(id: Long): EveEsiApi.Corporation? {
        return try {
            api.getCorporation(id)
        } catch (e: FeignException) {
            null
        }
    }

    fun getCharacter(id: Long): EveEsiApi.Character? {
        return try {
            api.getCharacter(id)
        } catch (e: FeignException) {
            null
        }
    }

    private lateinit var api: EveEsiApi

    @Value("\${eve.api.url}")
    private lateinit var url: String

    @PostConstruct
    fun init() {
        api = Feign.builder().decoder(GsonDecoder()).encoder(GsonEncoder()).target(EveEsiApi::class.java, url)
    }







}