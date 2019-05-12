package com.example.dao

import org.jetbrains.exposed.sql.*

object Events: Table(){
    val id = integer("id").primaryKey()
    val name = text("name").uniqueIndex()
    val desc = text("description")
    val date = text("date")
}
