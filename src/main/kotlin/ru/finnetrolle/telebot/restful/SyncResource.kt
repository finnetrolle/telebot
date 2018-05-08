package ru.finnetrolle.telebot.restful

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import ru.finnetrolle.telebot.service.internal.PilotService

/**
 * Licence: MIT
 * Legion of xXDEATHXx notification bot for telegram
 * Created by maxsyachin on 12.04.16.
 */

@Controller
@RequestMapping("/test")
class SyncResource {

    @Autowired
    private lateinit var pilotService: PilotService

    private val log = LoggerFactory.getLogger(SyncResource::class.java)

    @RequestMapping(method = arrayOf(RequestMethod.GET), path = arrayOf("/sync"))
    @ResponseBody
    fun amnesty(): SyncWrapper {
        val start = System.currentTimeMillis()
        val result = pilotService.syncEveApi()
        val wrapper = SyncWrapper(System.currentTimeMillis() - start, result)
        log.info("Sync result: $wrapper")
        return wrapper
    }

    data class SyncWrapper( val time: Long, val result: PilotService.SyncResult)

    @ExceptionHandler(IllegalAccessException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun interceptUnauthorized() {}

}