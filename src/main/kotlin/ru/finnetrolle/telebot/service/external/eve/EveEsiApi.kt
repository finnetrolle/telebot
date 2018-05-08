package ru.finnetrolle.telebot.service.external.eve

import feign.Param
import feign.RequestLine

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */
interface EveEsiApi {

    data class Affiliation (
            val alliance_id: Long,
            val character_id: Long,
            val corporation_id: Long
    )
    @RequestLine("POST /latest/characters/affiliation/?datasource=tranquility")
    fun getAffiliation(pilotIds: Set<Long>): Set<Affiliation>


    data class Alliance (
            val creator_corporation_id : Long,
            val creator_id : Long,
            val date_founded : String,
            val executor_corporation_id : Long,
            val name : String,
            val ticker : String
    )
    @RequestLine("GET /latest/alliances/{id}/")
    fun getAlliance(@Param("id") id: Long): Alliance

    data class Corporation (
            val alliance_id: Long,
            val ceo_id: Long,
            val creator_id: Long,
            val date_founded: String,
            val description: String,
            val member_count: Int,
            val name: String,
            val tax_rate: String,
            val ticker: String,
            val url: String
    )
    @RequestLine("GET /latest/corporations/{corporation_id}/")
    fun getCorporation(@Param("corporation_id") id: Long): Corporation

    data class Character(
            val ancestry_id: Long,
            val birthday: String,
            val bloodline_id: Long,
            val corporation_id: Long,
            val description: String,
            val gender: String,
            val name: String,
            val race_id: Int
    )
    @RequestLine("GET /latest/characters/{id}/")
    fun getCharacter(@Param("id") id: Long): Character

}