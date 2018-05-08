package ru.finnetrolle.telebot.service.processing.commands.secured

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.finnetrolle.telebot.model.Pilot
import ru.finnetrolle.telebot.service.internal.AllyService
import ru.finnetrolle.telebot.service.processing.commands.AbstractSecuredCommand
import ru.finnetrolle.telebot.util.MessageLocalization

/**
 * Telegram bot
 * Licence: MIT
 * Author: Finne Trolle
 */

@Component
class AddAllianceCommand : AbstractSecuredCommand() {

    @Autowired
    private lateinit var allyService: AllyService

    @Autowired
    private lateinit var loc: MessageLocalization

    override fun name() = "/ADDALLY"

    override fun description() = loc.getMessage("telebot.command.description.addally")

    override fun execute(pilot: Pilot, data: String): String {
        return try {
            val result = allyService.add(data.toLong())
            when (result) {
                is AllyService.Add.Success -> loc.getMessage("messages.ally.added", result.alliance.title)
                is AllyService.Add.AlreadyExists -> loc.getMessage("messages.ally.in.list")
                is AllyService.Add.NotFound -> loc.getMessage("messages.ally.not.exist")
            }
        } catch (e: NumberFormatException) {
            loc.getMessage("messages.ally.id.required")
        }
    }


}