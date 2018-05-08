package ru.finnetrolle.telebot.restful

import org.hibernate.validator.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import ru.finnetrolle.telebot.model.Pilot
import ru.finnetrolle.telebot.service.processing.commands.secured.GroupBroadcastCommand
import ru.finnetrolle.telebot.service.processing.commands.unsecured.GlobalBroadcasterCommand
import javax.validation.Validation

/**
 * Licence: MIT
 * Legion of xXDEATHXx notification bot for telegram
 * Created by finnetrolle on 13.03.16.
 */
@Controller
class BroadcastResource {

    @Autowired
    private lateinit var globalCaster: GlobalBroadcasterCommand

    @Autowired
    private lateinit var groupCaster: GroupBroadcastCommand

    @Value("\${api.secret.cast.group}")
    private lateinit var groupCastSecret: String

    @Value("\${api.secret.cast.global}")
    private lateinit var globalCastSecret: String

    sealed class In {
        class Message(
                @NotBlank val text: String,
                @NotBlank val secret: String,
                @NotBlank val from: String) : In()

        class GroupMessage(
                @NotBlank val text: String,
                @NotBlank val group: String,
                @NotBlank val secret: String,
                @NotBlank val from: String) : In()
    }

    @RequestMapping(value = "/cast", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun cast(@RequestBody message: In.Message): ResponseEntity<String> {
        validate(message)?.let { return it }
        return ResponseEntity.ok(globalCaster.execute(Pilot(characterName = message.from), message.text))
    }

    @RequestMapping(value = "/gc", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun groupcast(@RequestBody message: In.GroupMessage): ResponseEntity<String> {
        validate(message)?.let { return it }
        return ResponseEntity.ok(groupCaster.execute(Pilot(characterName = message.from), "${message.group} ${message.text}"))
    }

    private fun validate(input: In): ResponseEntity<String>? {
        val constraints = Validation.buildDefaultValidatorFactory().validator!!.validate(input)
        return if (constraints.isNotEmpty()) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(constraints.map { it.message }.joinToString("\n"))
        } else {
            if ((input is In.GroupMessage && !input.secret.equals(groupCastSecret))
                    || (input is In.Message && !input.secret.equals(globalCastSecret))) {
                ResponseEntity.status(HttpStatus.FORBIDDEN).body("")
            } else {
                null
            }
        }
    }

}