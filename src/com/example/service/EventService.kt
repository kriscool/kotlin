package com.example.service

import com.example.dao.Events
import com.example.model.Event
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


class EventService {
    fun getAllEvents(): ArrayList<Event> {
        var list = arrayListOf<Event>()
        transaction {
            for (e in Events.selectAll().iterator()) {
                list.add(Event(e.data[0] as Int, e.data[1].toString(), e.data[2] as String, e.data[3] as String))
            }
        }
        return list
    }

   /*fun getEvent(id: Int?): Event? {
        return transaction {
            Events.select { Events.id eq id }
                .map { Event(it[Events.id], it[Events.name], it[Events.desc], it[Events.date]) }
        }.getOrNull(0)
    }*/


}
