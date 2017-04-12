package ru.finnetrolle.telebot.service.processing.commands.unsecured

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.finnetrolle.telebot.model.Broadcast
import ru.finnetrolle.telebot.model.BroadcastRepository
import ru.finnetrolle.telebot.model.Pilot
import ru.finnetrolle.telebot.service.external.TranslateService
import ru.finnetrolle.telebot.service.internal.PilotService
import ru.finnetrolle.telebot.service.internal.QuestService
import ru.finnetrolle.telebot.service.processing.commands.AbstractUnsecuredCommand
import ru.finnetrolle.telebot.service.telegram.TelegramBotService
import ru.finnetrolle.telebot.util.CommandParser
import ru.finnetrolle.telebot.util.MessageBuilder
import ru.finnetrolle.telebot.util.MessageLocalization
import java.util.*

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */

@Component
class QuestCommand : AbstractUnsecuredCommand() {

    @Autowired
    private lateinit var pilotService: PilotService

    @Autowired
    private lateinit var telegram: TelegramBotService

    @Autowired
    private lateinit var loc: MessageLocalization

    @Autowired
    private lateinit var quest: QuestService

    private val log = LoggerFactory.getLogger(QuestCommand::class.java)

    override fun name() = "/QUEST"

    override fun description() = loc.getMessage("telebot.command.description.quest")

    override fun execute(pilot: Pilot, data: String): String {
        if (!pilot.moderator && !pilot.speaker) {
            return loc.getMessage("message.cast.you.not.speaker")
        }
        val qc = CommandParser.parseQuestData(data)
        val users = if (qc.groupName.toUpperCase() == "ALL") {
            pilotService.getLegalUsers()
        } else {
            pilotService.getLegalUsers(qc.groupName)
        }
        val q = quest.create(pilot, qc.text, qc.options, qc.mins, qc.groupName)
        try {
            val message = createQuestMessage(q, pilot)
            telegram.broadcast(users.map { MessageBuilder.build(it.id.toString(), message) })
            return loc.getMessage("vote.created", q.id)
        } catch (e: Exception) {
            log.error("Can't execute command global broadcast because of", e)
        }
        return "Some very bad happened"
    }

    private fun createQuestMessage(rep : QuestService.QuestRepresentation, pilot: Pilot): String {
        val ops = rep.options.map { "${it.text} - /vote_quest_${it.id}" }.joinToString("\n\n")
        return loc.getMessage("vote.message",pilot.characterName, rep.text, ops)
    }

}