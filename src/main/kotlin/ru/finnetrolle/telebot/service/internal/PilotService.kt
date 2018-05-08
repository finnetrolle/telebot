package ru.finnetrolle.telebot.service.internal

import com.beimin.eveapi.model.eve.CharacterAffiliation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.api.objects.User
import ru.finnetrolle.telebot.model.Pilot
import ru.finnetrolle.telebot.model.PilotRepository
import ru.finnetrolle.telebot.service.external.ExternalGroupProvider
import ru.finnetrolle.telebot.service.external.eve.EveEsiApi
import ru.finnetrolle.telebot.service.external.eve.EveEsiApiConnector
import ru.finnetrolle.telebot.util.EveApiUnknownException
import ru.finnetrolle.telebot.util.decide
import java.util.*
import javax.annotation.PostConstruct

/**
 * Licence: MIT
 * Legion of xXDEATHXx notification bot for telegram
 * Created by finnetrolle on 13.03.16.
 */

@Component
open class PilotService {

    @Autowired lateinit private var pilotRepo: PilotRepository
    @Autowired lateinit private var allyService: AllyService
    @Autowired lateinit private var corpService: CorpService
    @Autowired lateinit private var eve: EveEsiApiConnector
    @Autowired lateinit private var groups: ExternalGroupProvider

    @Value("\${telebot.superuser}")
    private lateinit var superUser: String

    private val log = LoggerFactory.getLogger(PilotService::class.java)

    @PostConstruct
    @Transactional
    open fun init() {
        log.info("System bot super user is $superUser")
        setModerator(superUser, true).decide({
            log.info("Succesfully promoted super user is ${it.characterName}")
        }, {
            log.info("Super User is not registered in system")
        })
    }

    interface Add {
        data class Success(val pilot: Pilot) : Add
        data class AlreadyExists(val pilot: Pilot) : Add
    }

    @Transactional
    open fun add(user: User, character: String, characterId: Long): Add {
        log.info("registering new pilot: " + character)
        return pilotRepo.findOne(user.id).decide({
            Add.AlreadyExists(it)
        },{
            Add.Success(pilotRepo.save(Pilot(user.id, user.firstName, user.lastName, user.userName, character, characterId)))
        })
    }

    open fun getLegalUsers(): List<Pilot> = pilotRepo.findByRenegadeFalse()

    open fun getLegalUsers(groupName: String): List<Pilot> {
        val names = groups.getMembers(groupName)
        return if (names.isEmpty()) {
            listOf<Pilot>()
        } else
            pilotRepo.findByRenegadeFalse().filter { names.contains(it.characterName) }
    }

    open fun getPilot(telegramId: Int) = pilotRepo.findOne(telegramId)
    open fun getPilot(characterName: String) = pilotRepo.findByCharacterName(characterName)

    data class CheckPair(val pilot: Pilot, val character: CharacterAffiliation)
    data class CheckResult(val renegaded: List<String>, val checked: Int)

//    @Transactional
//    open fun check(): CheckResult {
//        val allowedAllies = allyService.getAll().map { a -> a.id }.toSet()
//        val allowedCorps = corpService.getAll().map { c -> c.id }.toSet()
//        val pilotsToCheck = pilotRepo.findByRenegadeFalse().filter { !isSuperUser(it) }
//        val afillations = eve.getAffilations(pilotsToCheck.map { it.characterId })
//        val renegades = pilotsToCheck
//                .map { CheckPair(it, afillations.getOrElse(it.characterId, {CharacterAffiliation()})) }
//                .filter { !allowedAllies.contains(it.character.allianceID) }
//                .filter { !allowedCorps.contains(it.character.corporationID) }
//        if (renegades.isNotEmpty()) {
//            pilotRepo.makeRenegades(renegades.map { it.pilot.id })
//        }
//        return CheckResult(renegades.map { it.pilot.characterName }, pilotsToCheck.size)
//    }
//
//    @Transactional
//    open fun amnesty(): CheckResult {
//        val allowedAllies = allyService.getAll().map { a -> a.id }.toSet()
//        val allowedCorps = corpService.getAll().map { c -> c.id }.toSet()
//        val pilotsToCheck = pilotRepo.findByRenegadeTrue().filter { !isSuperUser(it) }
//        val afillations = eve.getAffilations(pilotsToCheck.map { it.characterId })
//        val amnestee = pilotsToCheck
//                .map { CheckPair(it, afillations.getOrElse(it.characterId, {CharacterAffiliation()})) }
//                .filter { allowedAllies.contains(it.character.allianceID ?: -1) || allowedCorps.contains(it.character.corporationID) }
//        if (amnestee.isNotEmpty()) {
//            pilotRepo.makeAmnestee(amnestee.map { it.pilot.id })
//        }
//        return CheckResult(amnestee.map { it.pilot.characterName }, pilotsToCheck.size)
//    }

    open fun getCorpMates(pilot: Pilot): List<Pilot> {
        if (pilot.corpId == 0L) {
            return listOf()
        }
        return pilotRepo.findCorpMates(pilot.corpId)
    }

    @Transactional
    open fun syncEveApi(): SyncResult {
        val allowedAllies = allyService.getAll().map { it.id }.toSet()
        val allowedCorps = corpService.getAll().map { it.id }.toSet()
        val pilots = pilotRepo.findAll()
        val afillations = eve.getAffiliations(pilots.map { it.characterId }.toSet())

        val renegaded = mutableListOf<String>()
        val amnested = mutableListOf<String>()
        val corpChanged = mutableListOf<String>()
        pilots.forEach {
            val afillation = afillations[it.characterId]
            if (afillation != null) {
                var mod = false
                val renegade = !allowedAllies.contains(afillation.alliance_id) && !allowedCorps.contains(afillation.corporation_id)
                if (it.renegade != renegade && !isSuperUser(it)) {
                    mod = true
                    it.renegade = renegade
                    if (renegade) {
                        renegaded.add(it.characterName)
                    } else {
                        amnested.add(it.characterName)
                    }
                }
                if (it.corpId != afillation.corporation_id) {
                    mod = true
                    it.corpId = afillation.corporation_id
                    corpChanged.add(it.characterName)
                }
                if (mod) {
                    pilotRepo.save(pilot = it)
                }
            }
        }

        return SyncResult(renegaded, amnested, corpChanged, pilots.size)
    }

    data class SyncResult(val renegaded: List<String>, val amnested: List<String>, val corpChanged: List<String>, val checked: Int)

    sealed class SingleCheckResult {
        class OK(val name: String, val corp: String, val ally: String) : SingleCheckResult()
        class Renegade(val name: String, val corp: String, val ally: String) : SingleCheckResult()
    }

    open fun singleCheck(characterId: Long): SingleCheckResult {
        log.info("Checking id = $characterId")
        eve.getAffiliations(setOf(characterId))[characterId]?.let {
            val char = eve.getCharacter(characterId) ?: EveEsiApi.Character(0L, "", 0L, 0L, "", "", "", 0)
            val ally = if (it.alliance_id != 0L) eve.getAlliance(it.alliance_id)!!.name else ""
            val corp = eve.getCorporation(it.corporation_id)!!.name
            if (allyService.get(it.alliance_id).isPresent ||
                corpService.get(it.corporation_id).isPresent) {
                SingleCheckResult.OK(char.name, corp, ally)
            } else {
                SingleCheckResult.Renegade(char.name, corp, ally )
            }
        }
        throw EveApiUnknownException() // external system not answering
    }

    @Transactional
    open fun setModerator(name: String, value: Boolean): Optional<Pilot> {
        return pilotRepo.findByCharacterName(name).decide({
            it.moderator = value
            Optional.of(pilotRepo.save(it))
        },{
            Optional.empty()
        })
    }

    @Transactional
    open fun setRenegade(name: String, value: Boolean): Optional<Pilot> {
        return pilotRepo.findByCharacterName(name).decide({
            it.renegade = value
            Optional.of(pilotRepo.save(it))
        },{
            Optional.empty()
        })
    }

    @Transactional
    open fun remove(name: String): Optional<Pilot> {
        return pilotRepo.findByCharacterName(name).decide({
            try {
                pilotRepo.dropPilotQuestOptions(it.id)
                pilotRepo.delete(it)
                Optional.of(it)
            } catch (e: Exception) {
                log.error("Can't remove pilot because of bug", e)
                Optional.empty()
            }
        },{
            Optional.empty()
        })
    }

    @Transactional
    open fun remove(id: Int): Optional<Pilot> {
        return pilotRepo.findOne(id).decide({
            try {
                pilotRepo.dropPilotQuestOptions(it.id)
                pilotRepo.delete(it)
                Optional.of(it)
            } catch (e: Exception) {
                log.error("Can't remove pilot because of bug", e)
                Optional.empty()
            }
        },{
            Optional.empty()
        })
    }

    @Transactional
    open fun dropRenegades(): List<Pilot> {
        val pilots = pilotRepo.findByRenegadeTrue()
        pilotRepo.dropRenegades()
        return pilots
    }

    @Transactional
    open fun setTranslator(id: Int, language: String): Optional<Pilot> {
        return pilotRepo.findOne(id).decide({
            it.translateTo = language
            Optional.of(pilotRepo.save(it))
        },{
            Optional.empty()
        })
    }

    @Transactional
    open fun update(pilot: Pilot): Pilot {
        return pilotRepo.save(pilot)
    }

    @Transactional
    open fun setSpeaker(name: String, value: Boolean): Optional<Pilot> {
        return pilotRepo.findByCharacterName(name).decide({
            it.speaker = value
            Optional.of(pilotRepo.save(it))
        },{
            Optional.empty()
        })
    }

    open fun getModerators() = pilotRepo.findByModeratorTrue()

    private fun isSuperUser(pilot: Pilot) = pilot.characterName == superUser

    open fun getAllUsers() = pilotRepo.findAll()

    open fun getSpeakers() = pilotRepo.findBySpeakerTrue()

}