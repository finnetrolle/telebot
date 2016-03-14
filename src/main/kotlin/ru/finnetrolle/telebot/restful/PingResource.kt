package ru.finnetrolle.telebot.restful

import org.springframework.stereotype.Component
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.Response

/**
* Licence: MIT
* Legion of xXDEATHXx notification bot for telegram
* Created by finnetrolle on 14.03.16.
*/

@Component
@Path("ping")
class PingResource {

    @GET
    fun ping(): Response {
        return Response.ok("OK").build()
    }

}