package ru.finnetrolle.telebot.restful

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import ru.finnetrolle.telebot.model.Mail
import ru.finnetrolle.telebot.service.external.MailbotService
import ru.finnetrolle.telebot.service.internal.PilotService

/**
 * Licence: MIT
 * Legion of xXDEATHXx notification bot for telegram
 * Created by maxsyachin on 12.04.16.
 */

@Controller
@RequestMapping("/test")
class MailResource {

    @Autowired lateinit private var mailbot: MailbotService

    @Autowired
    private lateinit var pilotService: PilotService

    @Value("\${api.secret.utils}")
    private lateinit var lSecret: String

    private val log = LoggerFactory.getLogger(MailResource::class.java)

    @RequestMapping(method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun mail(): ResponseEntity<List<Mail>> {
        mailbot.receiveMail()
        return ResponseEntity.ok(mailbot.getLast())
    }

//    @RequestMapping(method = arrayOf(RequestMethod.GET), path = arrayOf("/check"))
//    @ResponseBody
//    fun check(@RequestHeader("Secret") secret: String): SyncWrapper {
//        if (!lSecret.equals(secret)) {
//            throw IllegalAccessException()
//        }
//        val start = System.currentTimeMillis()
////        val result = pilotService.check()
//        val result = pilotService.syncEveApi()
//        val wrapper = SyncWrapper(System.currentTimeMillis() - start, result)
//        log.info("Renegade check result: $wrapper")
//        return wrapper
//    }
//
//    @RequestMapping(method = arrayOf(RequestMethod.GET), path = arrayOf("/amnesty"))
//    @ResponseBody
//    fun amnesty(@RequestHeader("Secret") secret: String): SyncWrapper {
//        if (!lSecret.equals(secret)) {
//            throw IllegalAccessException()
//        }
//        val start = System.currentTimeMillis()
////        val result = pilotService.amnesty()
//        val result = pilotService.syncEveApi()
//        val wrapper = SyncWrapper(System.currentTimeMillis() - start, result)
//        log.info("Renegade amnesty result: $wrapper")
//        return wrapper
//    }

    @RequestMapping(method = arrayOf(RequestMethod.GET), path = arrayOf("/sync"))
    @ResponseBody
    fun amnesty(): SyncWrapper {
        val start = System.currentTimeMillis()
        val result = pilotService.syncEveApi()
        val wrapper = SyncWrapper(System.currentTimeMillis() - start, result)
        log.info("Sync result: $wrapper")
        return wrapper
    }

//    data class AmnestyWrapper( val time: Long, val result: PilotService.CheckResult)
//    data class CheckWrapper( val time: Long, val result: PilotService.CheckResult)
    data class SyncWrapper( val time: Long, val result: PilotService.SyncResult)

    @ExceptionHandler(IllegalAccessException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun interceptUnauthorized() {}

}