package ru.finnetrolle.telebot.service.processing.commands.unsecured

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.finnetrolle.telebot.model.Pilot
import ru.finnetrolle.telebot.model.PilotRepository
import ru.finnetrolle.telebot.service.internal.PilotService
import ru.finnetrolle.telebot.service.processing.commands.AbstractUnsecuredCommand
import ru.finnetrolle.telebot.service.telegram.TelegramBotService
import ru.finnetrolle.telebot.util.EveTime
import ru.finnetrolle.telebot.util.MessageBuilder
import ru.finnetrolle.telebot.util.MessageLocalization

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */
@Component
class CorpBroadcasterCommand: AbstractUnsecuredCommand() {

    @Autowired
    private lateinit var loc: MessageLocalization

    @Autowired
    private lateinit var telegram: TelegramBotService

    @Autowired
    private lateinit var pilotRepo: PilotRepository

    private val log = LoggerFactory.getLogger(CorpBroadcasterCommand::class.java)

    override fun name() = "/CORP"

    override fun description() = loc.getMessage("telebot.command.description.corp")

    override fun execute(pilot: Pilot, data: String): String {
        if (pilot.corpId == 0L) {
            return loc.getMessage("messages.impossible")
        }
        val pilots = pilotRepo.findCorpMates(pilot.corpId)
        val message = "Corporate broadcast from ${pilot.characterName} at ${EveTime.now()} \n$data"
        try {
            telegram.broadcast(pilots
                    .map { u -> MessageBuilder.build(u.id.toString(), message) }
            )
            return loc.getMessage("messages.broadcast.result", pilots.size)
        } catch (e: Exception) {
            log.error("Can't execute corporate broadcast because of", e)
        }
        return "Some very bad happened"
    }
}