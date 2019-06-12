package com.example.service

import com.example.dao.Users
import com.example.model.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt


class UserService {

    fun getUserByName(name: String): User? {
        return transaction {
            Users.select { Users.name eq name }
                .map {
                    User(
                        it[Users.id], it[Users.name], it[Users.password], it[Users.address]
                    )
                }
        }.getOrNull(0)
    }

     fun insertUser(user: User): User {
        val userId = transaction {
            Users.insert {
                it[Users.name] = user.name
                it[Users.password] = user.password
                it[Users.address] = user.address
            }.generatedKey
        }
        return user.copy(id = userId!!.toInt())
    }

    fun checkIfExistsAndLogin(name: String, password: String): Boolean {
        val user: User? = getUserByName(name)
        return if (user != null) BCrypt.checkpw(password, user.password)
        else
            false
    }

    fun updateUser(user: User) {
        transaction {
            Users.update({ Users.id eq user.id }) {
                it[password] = user.password
            }
        }
    }


}
