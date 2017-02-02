package ru.finnetrolle.telebot.service.internal

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.finnetrolle.telebot.model.Corporation
import ru.finnetrolle.telebot.model.CorporationRepository
import ru.finnetrolle.telebot.service.external.EveApiConnector
import ru.finnetrolle.telebot.util.decide

/**
 * Licence: MIT
 * Legion of xXDEATHXx notification bot for telegram
 * Created by maxsyachin on 19.03.16.
 */

@Component
open class CorpService {

    @Autowired
    private lateinit var repo: CorporationRepository

    @Autowired
    private lateinit var eve: EveApiConnector

    interface Add {
        data class Success(val corporation: Corporation) : Add
        data class AlreadyInList(val corporation: Corporation) : Add
        data class NotExist(val id: Long) : Add
    }

    @Transactional
    open fun add(id: Long): Add {
        val exists = repo.findOne(id)
        if (exists.isPresent) {
            return Add.AlreadyInList(exists.get())
        }

        val corp = eve.getCorporation(id)
        if (corp == null) {
            return Add.NotExist(id)
        } else {
            return Add.Success(repo.save(Corporation(corp.corporationID, corp.ticker, corp.corporationName)))
        }

    }

    interface Remove {
        data class Success(val corporation: Corporation) : Remove
        data class NotFound(val ticker: String) : Remove
    }

    @Transactional
    open fun remove(ticker: String): Remove {
        return repo.findByTicker(ticker).decide({
            repo.delete(it)
            Remove.Success(it)
        },{
            Remove.NotFound(ticker)
        })
    }

    open fun get(corpId: Long) = repo.findOne(corpId)

    open fun getAll() = repo.findAll()

}