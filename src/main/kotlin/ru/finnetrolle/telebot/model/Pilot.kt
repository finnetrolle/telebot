package ru.finnetrolle.telebot.model

import org.springframework.data.repository.CrudRepository
import javax.persistence.Entity
import javax.persistence.Id

/**
 * Created by maxsyachin on 13.03.16.
 */

interface PilotRepository: CrudRepository<Pilot, Int>

@Entity(name = "pilots")
data class Pilot (
        @Id var id: Int = 0,
        var firstName: String? = "",
        var lastName: String? = "",
        var username: String = "",
        var apiKey: Int = 0,
        var vCode: String = "",
        var characterName: String = "",
        var characterId: Long = 0
)