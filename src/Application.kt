package com.example

import com.example.com.example.dao.CarUsers
import com.example.com.example.dao.Cars
import com.example.dao.Events
import com.example.dao.Users
import com.example.model.User
import com.example.service.EventService
import com.example.service.UserService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import freemarker.cache.*
import io.ktor.freemarker.*
import io.ktor.auth.*
import io.ktor.features.StatusPages
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.lang.Exception

//import io.ktor.client.features.auth.basic.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

//@Suppress("unused") // Referenced in application.conf
//@kotlin.jvm.JvmOverloads
fun Application.module() {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(StatusPages) {
        status(
            HttpStatusCode.InternalServerError,
            HttpStatusCode.NotFound
        ) {
            call.respond(FreeMarkerContent("error.ftl",  mapOf("error" to ""), ""))
        }
    }

    install(Authentication) {
        basic("auth") {
            validate { if (UserService().loginUser(it.name, it.password)) UserIdPrincipal(it.name) else null }
        }
    }

    initDataBase()

    routing {
        //main page
        get("/") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("log" to 1), ""))
        }

        //register
        route("/register") {
            get {
                call.respond(FreeMarkerContent("register.ftl", mapOf("error" to ""), ""))
            }

            post {
                var params = call.receiveParameters()
                var name: String = params["name"].toString()
                var pass = params["password"].toString()
                var pass2 = params["password2"].toString()
                var address = params["email"].toString()
                var error: String

                if (name.length > 4) {
                    when (Validator().validate(pass, pass2)) {
                        ErrorsRegister.PASS_TOO_SHORT -> error = "Password is too short"
                        ErrorsRegister.NO_CAPITAL_LETTER -> error = "The password must have one capital letter"
                        ErrorsRegister.NO_SPECIAL_SIGN -> error = "The password must have one special sign"
                        ErrorsRegister.PASSWORD_NOT_MATCH -> error = "Passwords didn't match"
                        ErrorsRegister.OK -> {
                            error = "User registered!"
                            try {
                                UserService().registerUser(name, pass, address)
                            } catch (e: Exception) {
                                if (e.message!!.contains("users_name_unique"))
                                    error = "User already taken"
                            }
                        }
                    }
                } else error = "Login should have 5 letters at least"

                call.respond(FreeMarkerContent("register.ftl", mapOf("error" to error), ""))
            }
        }

        authenticate("auth") {
            get("/protected") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respond(FreeMarkerContent("indexLogged.ftl", mapOf("user" to UserService().getUserFromName(principal.name)), ""))
            }
            get("/protected/details") {
                val u = UserService().getUserFromName(call.principal<UserIdPrincipal>()!!.name)
                call.respond(
                    FreeMarkerContent(
                        "userDetails.ftl",
                        mapOf("error" to "", "login" to u!!.name, "address" to u!!.address),
                        ""
                    )
                )
            }
            post("/protected/details") {
                val params = call.receiveParameters()
                val pass: String = params["password"].toString()
                val pass2: String = params["password2"].toString()
                val address: String = params["email"].toString()
                val oldPass: String = params["oldPassword"].toString()
                val user = UserService().getUserFromName(call.principal<UserIdPrincipal>()!!.name)
                var error: String = ""
                try {
                    if (BCrypt.checkpw(oldPass, user!!.password)) {
                        if (address.isNotEmpty() && pass.isNotEmpty()) {
                            error = when (Validator().validate(pass, pass2)) {
                                ErrorsRegister.PASS_TOO_SHORT -> "Password is too short"
                                ErrorsRegister.NO_CAPITAL_LETTER -> "The password must have one capital letter"
                                ErrorsRegister.NO_SPECIAL_SIGN -> "The password must have one special sign"
                                ErrorsRegister.PASSWORD_NOT_MATCH -> "Passwords didn't match"
                                ErrorsRegister.OK -> {
                                    val temp: User =
                                        User(user.id, user.name, BCrypt.hashpw(pass, BCrypt.gensalt()), address, user.event)
                                    UserService().updateUser(temp)
                                    "User updated!"
                                }
                            }
                        } else if (address.isNotEmpty() && !pass.isNotEmpty()) {
                            val temp: User = User(user.id, user.name, user.password, address, user.event)
                            UserService().updateUser(temp)
                            error = "User updated!"
                        } else if (!address.isNotEmpty() && pass.isNotEmpty()) {
                            error = when (Validator().validate(pass, pass2)) {
                                ErrorsRegister.PASS_TOO_SHORT -> "Password is too short"
                                ErrorsRegister.NO_CAPITAL_LETTER -> "The password must have one capital letter"
                                ErrorsRegister.NO_SPECIAL_SIGN -> "The password must have one special sign"
                                ErrorsRegister.PASSWORD_NOT_MATCH -> "Passwords didn't match"
                                ErrorsRegister.OK -> {
                                    val temp: User =
                                        User(user.id, user.name, BCrypt.hashpw(pass, BCrypt.gensalt()), user.address, user.event)
                                    UserService().updateUser(temp)
                                    "User updated!"
                                }
                            }
                        }
                    } else error = "Wrong password"
                } catch (e: Exception) {
                    error = "Something go wrong"
                }
                val newU = UserService().getUserFromName(call.principal<UserIdPrincipal>()!!.name)

                call.respond(
                    FreeMarkerContent(
                        "userDetails.ftl",
                        mapOf("error" to error, "login" to newU!!.name, "address" to newU!!.address),
                        ""
                    )
                )
            }
            get("/protected/events") {
                call.respond(FreeMarkerContent("eventList.ftl", mapOf("events" to EventService().getAllEvents()), ""))
            }
            get("/protected/events/{id}") {
                call.respond(
                    FreeMarkerContent(
                        "eventDetails.ftl",""
                      //  mapOf("item" to EventService().getEvent(call.parameters["id"]!!.toInt())), ""
                    )
                )
            }
            get("/protected/events/{id}/register"){
                var user: User = UserService().getUserFromName(call.principal<UserIdPrincipal>()!!.name)!!
                //user.event = EventService().getEvent(call.parameters["id"]!!.toInt())!!
                UserService().updateUser(user)
                call.respond(FreeMarkerContent("indexLogged.ftl", mapOf("user" to UserService().getUserFromName(user.name)), ""))
            }
            get("/protected/events/{id}/unsubscribe"){
                var user: User = UserService().getUserFromName(call.principal<UserIdPrincipal>()!!.name)!!
                user.event = null
                UserService().updateUser(user)
                call.respond(FreeMarkerContent("indexLogged.ftl", mapOf("user" to UserService().getUserFromName(user.name)), ""))
            }
        }
    }
}


fun initDataBase() {
    val hikariConfig = HikariConfig("/hikari.properties")
    val hikariDataSource = HikariDataSource(hikariConfig)
    Database.connect(hikariDataSource)
    transaction {
        create(Events)
        create(Users)
        create(Cars)
        create(CarUsers)
    }
}

data class IndexData(val items: List<Int>)


