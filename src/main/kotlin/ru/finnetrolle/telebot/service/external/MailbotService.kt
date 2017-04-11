package ru.finnetrolle.telebot.service.external

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.finnetrolle.telebot.model.Mail
import ru.finnetrolle.telebot.model.MailRepository
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct

/**
 * Licence: MIT
 * Legion of xXDEATHXx notification bot for telegram
 * Created by maxsyachin on 12.04.16.
 */

@Component
open class MailbotService {

    @Autowired lateinit private var eve: EveApiConnector

    @Autowired lateinit private var mailRepo: MailRepository

    @Value("\${mailbot.keyid}")
    private var keyId: Int = 0

    @Value("\${mailbot.vcode}")
    private lateinit var vCode: String

    @Value("\${mailbot.list.id}")
    private lateinit var listId: String

    @Value("\${mailbot.alive}")
    private var alive: Boolean = false

    fun isAlive() = alive

    private var lastId: AtomicLong = AtomicLong()

    @PostConstruct
    open fun init() {
        if (alive) {
            lastId.set(mailRepo.getMaxId() ?: 0)
            log.info("Last mail id is ${lastId.get()}")
        }
    }

    open fun getLast(): List<Mail> {
        return if (alive) mailRepo.findFirst3ByOrderByIdDesc() else listOf<Mail>()
    }

    @Transactional
    open fun receiveMail() {
        if (!alive) {
            return
        }
        log.info("Receiving mail procedure")
        val mails = eve.getMailList(keyId.toInt(), vCode, listId)
                .filter { x -> x.messageID > lastId.get() }
                .filter { x -> x.title.contains("CTA") || x.title.contains("СТА") }
                .map { x ->
                    Mail(x.messageID,
                            x.sentDate,
                            eve.getCharacter(x.senderID).characterName,
                            x.title,
                            purgeHtml(eve.getMailBody(keyId.toInt(), vCode, x.messageID)))
                }
        if (mails.isNotEmpty()) {
            lastId.set(mails.maxBy { x -> x.id }!!.id)
            mailRepo.save(mails)
        }
    }

    private fun purgeHtml(html: String): String {
        var text = html
        var pos = text.indexOf("<")
        while (pos != -1) {
            val end = text.indexOf(">")
            if (end == -1) {
                return text
            }
            text = clear(pos, end, text)
            pos = text.indexOf("<")
        }
        return text
    }

    private fun clear(from: Int, to: Int, text: String): String {
        val candidate = text.substring(from, to + 1)
        val replace = when (candidate) {
            "<br>" -> "\n"
            "<b>", "</b>" -> "*"
            "<i>", "</i>" -> "_"
            else -> ""
        }
        return text.substring(0, from) + replace + text.substring(to + 1, text.length)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MailbotService::class.java)
    }


}