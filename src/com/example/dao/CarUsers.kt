package com.example.com.example.dao


import com.example.dao.Users
import org.jetbrains.exposed.sql.*

object CarUsers: Table() {
    val car = reference("carId", Cars.id)
    val user = reference("userId", Users.id)
}
