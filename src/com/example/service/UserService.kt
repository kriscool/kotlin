package com.example.service

import com.example.dao.Users
import com.example.model.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt


class UserService {

    fun registerUser(name: String, pass: String, address: String) {
       // create(User(0, name, BCrypt.hashpw(pass, BCrypt.gensalt()), address, null))
    }

    fun getUser(id: Int): User? {
        return transaction {
            Users.select { Users.id eq id }
                .map {
                    User(
                        it[Users.id],
                        it[Users.name],
                        it[Users.password],
                        it[Users.address],
                       null
                    )
                }
        }.getOrNull(0)
    }

    fun getUserFromName(name: String): User? {
        return transaction {
            Users.select { Users.name eq name }
                .map {
                    User(
                        it[Users.id], it[Users.name], it[Users.password], it[Users.address], null
                    )
                }
        }.getOrNull(0)
    }

    private fun create(user: User): User {
        val userId = transaction {
            Users.insert {
                it[Users.name] = user.name
                it[Users.password] = user.password
                it[Users.address] = user.address
                it[Users.event] = null
            }.generatedKey
        }
        return user.copy(id = userId!!.toInt())
    }

    fun loginUser(name: String, password: String): Boolean {
        val temp: User? = getUserFromName(name)
        if (temp != null) return BCrypt.checkpw(password, temp.password)
        return false
    }

    fun updateUser(user: User) {
        transaction {
            Users.update({ Users.id eq user.id }) {
                it[password] = user.password
                it[address] = user.address
                if (user.event == null) {
                    it[event] = null
                } else {
                    it[event] = user.event!!.id
                }

            }
        }
    }


}
