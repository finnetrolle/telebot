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
import ru.finnetrolle.telebot.service.processing.commands.AbstractUnsecuredCommand
import ru.finnetrolle.telebot.service.telegram.TelegramBotService
import ru.finnetrolle.telebot.util.EveTime
import ru.finnetrolle.telebot.util.MessageBuilder
import ru.finnetrolle.telebot.util.MessageLocalization
import java.util.*

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */
@Component
open class GlobalBroadcasterCommand : AbstractUnsecuredCommand() {

    @Autowired
    private lateinit var pilotService: PilotService

    @Autowired
    private lateinit var telegram: TelegramBotService

    @Autowired
    private lateinit var loc: MessageLocalization

    @Autowired
    private lateinit var translator: TranslateService

    @Autowired
    private lateinit var castRepo: BroadcastRepository

    private val log = LoggerFactory.getLogger(GlobalBroadcasterCommand::class.java)

    override fun name() = "/CAST"

    override fun description() = loc.getMessage("telebot.command.description.cast")

    @Transactional
    override fun execute(pilot: Pilot, data: String): String {
        if (!pilot.moderator && !pilot.speaker) {
            return loc.getMessage("message.cast.you.not.speaker")
        }
        try {
            val message = "Broadcast from ${pilot.characterName} at ${EveTime.formatted()} \n$data"
            val users = pilotService.getLegalUsers()
            val languages = users
                    .filter { it.translateTo.isNotEmpty() }
                    .map { it.translateTo }
                    .distinct()
                    .map { Pair(it, translator.translate(message, it)) }
                    .toMap()
            telegram.broadcast(users
                    .map { u -> MessageBuilder.build(u.id.toString(), prepareMessage(u, languages, message)) }
            )
            castRepo.save(Broadcast(
                    fromName = pilot.characterName,
                    toGroupName = "all",
                    message = message,
                    receiversCount = users.size))
            return loc.getMessage("messages.broadcast.result", users.size)
        } catch (e: Exception) {
            log.error("Can't execute command global broadcast because of", e)
        }
        return "Some very bad happened"
    }

    fun prepareMessage(to: Pilot, languages: Map<String, String>, text: String) : String {
        if (to.translateTo.isNotEmpty()) {
            return "$text\n\n<=>\n\n${languages[to.translateTo!!]}"
        }
        return text
    }


}