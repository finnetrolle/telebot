package ru.finnetrolle.telebot.model

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.util.*
import javax.persistence.*


/**
* Licence: MIT
* Legion of xXDEATHXx notification bot for telegram
* Created by finnetrolle on 13.03.16.
*/

interface PilotRepository: Repository<Pilot, Int> {

    @Modifying
    @Query("update Pilot u set u.renegade = true where u.id in ?1")
    fun makeRenegades(@Param("ids") ids: Collection<Int>)

    @Modifying
    @Query("update Pilot u set u.renegade = false where u.id in ?1")
    fun makeAmnestee(@Param("ids") ids: Collection<Int>)

    @Modifying
    @Query("delete from pilots where renegade = true", nativeQuery = true)
    fun dropRenegades()

    @Modifying
    @Query("delete from pilot_options where id = ?1", nativeQuery = true)
    fun dropPilotQuestOptions(pilotTelegramId: Int)

    fun findByCharacterName(name: String): Optional<Pilot>

    fun findByRenegadeFalse(): List<Pilot>

    fun findByRenegadeTrue(): List<Pilot>

    fun findByModeratorTrue(): List<Pilot>

    fun findBySpeakerTrue(): List<Pilot>

    fun save(pilot: Pilot): Pilot

    fun findOne(id: Int): Optional<Pilot>

    fun delete(pilot: Pilot)

    fun findAll(): List<Pilot>
}

@Entity
@Table(name = "pilots")
data class Pilot (
        @Id var id: Int = 0, // telegram id is PK
        var firstName: String? = "",
        var lastName: String? = "",
        var username: String? = "",
        var characterName: String = "",
        var characterId: Long = 0,
        var moderator: Boolean = false,
        var renegade: Boolean = false,
        var translateTo: String = "",
        var speaker: Boolean = false,
        var corpId: Long = 0
)