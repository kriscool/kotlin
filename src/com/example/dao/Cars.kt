package com.example.com.example.dao

import org.jetbrains.exposed.sql.Table

object Cars: Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = text("name").uniqueIndex()
}
