package ru.finnetrolle.telebot.service.processing.commands.secured

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.finnetrolle.telebot.model.Broadcast
import ru.finnetrolle.telebot.model.BroadcastRepository
import ru.finnetrolle.telebot.model.Pilot
import ru.finnetrolle.telebot.service.internal.PilotService
import ru.finnetrolle.telebot.service.processing.commands.AbstractSecuredCommand
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
open class GroupBroadcastCommand : AbstractSecuredCommand() {

    @Autowired
    private lateinit var pilotService: PilotService

    @Autowired
    private lateinit var telegram: TelegramBotService

    @Autowired
    private lateinit var loc: MessageLocalization

    @Autowired
    private lateinit var castRepo: BroadcastRepository

    @Autowired
    private lateinit var eveTime: EveTime

    override fun name() = "/GC"

    override fun description() = loc.getMessage("telebot.command.description.gc")

    @Transactional
    override fun execute(pilot: Pilot, data: String): String {
        val text = data.substringAfter(" ")
        val groupName = data.substringBefore(" ")
        val users = pilotService.getLegalUsers(groupName)
        val message = "Group broadcast from ${pilot.characterName} to $groupName at ${eveTime.formatted()} \n$text"
        telegram.broadcast(users.map { u -> MessageBuilder.build(u.id.toString(), message) })
        castRepo.save(Broadcast(
                fromName = pilot.characterName,
                toGroupName = data.substringBefore(" "),
                message = message,
                receiversCount = users.size))
        return loc.getMessage("messages.broadcast.result", users.size)
    }
}