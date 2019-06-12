package com.example.dao

import org.jetbrains.exposed.sql.*

object Users: Table(){
    val id = integer("id").primaryKey().autoIncrement()
    val name = text("name").uniqueIndex()
    val password = text("password")
    val address = text("address")
}
