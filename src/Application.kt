package com.example

import com.example.com.example.dao.CarUsers
import com.example.com.example.dao.Cars
import com.example.com.example.model.Car
import com.example.com.example.service.CarService
import com.example.dao.Users
import com.example.model.User
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
            validate { if (UserService().checkIfExistsAndLogin(it.name, it.password)) UserIdPrincipal(it.name) else null }
        }
    }

    initDataBase()
    routing {
        get("/") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("log" to 1), ""))
        }

        route("/register") {
            get {
                call.respond(FreeMarkerContent("register.ftl", mapOf("error" to ""), ""))
            }

            post {
                var params = call.receiveParameters()
                var name: String = params["name"].toString()
                var pass = params["password"].toString()
                var address = params["email"].toString()
                var error: String

                error=validate(pass)
                if(error.equals("OK")) {
                    try {
                        UserService().insertUser(User(0, name, BCrypt.hashpw(pass, BCrypt.gensalt()), address))
                    } catch (e: Exception) {
                        if (e.message!!.contains("users_name_unique"))
                            error = "Nazwa użytkownika zajęta"
                    }
                }
                call.respond(FreeMarkerContent("register.ftl", mapOf("error" to error), ""))
            }
        }

        authenticate("auth") {
            get("/protected") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respond(FreeMarkerContent("indexLogged.ftl", mapOf("user" to UserService().getUserByName(principal.name)), ""))
            }
            get("/details") {
                val u = UserService().getUserByName(call.principal<UserIdPrincipal>()!!.name)
                call.respond(
                    FreeMarkerContent(
                        "userDetails.ftl",
                        mapOf("error" to "", "name" to u!!.name, "adres" to u!!.address),
                        ""
                    )
                )
            }
            get("/car") {
                call.respond(
                    FreeMarkerContent(
                        "car.ftl",
                        mapOf("error" to ""),
                        ""
                    )
                )
            }
            post("/car") {
                val params = call.receiveParameters()
                val name: String = params["name"].toString()
                CarService().createCar(Car(0,name))
                call.respond(
                    FreeMarkerContent(
                        "car.ftl",
                        mapOf("error" to "Dodano"),
                        ""
                    )
                )
            }

            get("/cars/all") {

                call.respond(
                    FreeMarkerContent(
                        "allCars.ftl",
                        mapOf("cars" to CarService().getAllCars()),
                        ""
                    )
                )
            }

            get("/edit") {
                call.respond(
                    FreeMarkerContent(
                        "editCars.ftl",
                        mapOf("cars" to CarService().getAllCars()),
                        ""
                    )
                )
            }

            post("/edit"){
                val params = call.receiveParameters()
                val idCar: Int = params["id"]!!.toInt()
                val nameCar: String = params["name"]!!.toString()
                CarService().updateCar(Car(idCar,nameCar))
                call.respond(
                    FreeMarkerContent(
                        "editCar.ftl",
                        mapOf("error" to "Edytowano","car" to CarService().getCar(idCar)),
                        ""
                    )
                )
            }

            get("/edit/{id}") {
                val id = call.parameters["id"]!!.toInt()
                val car:Car? = CarService().getCar(id)
                call.respond(
                    FreeMarkerContent(
                        "editCar.ftl",
                        mapOf("car" to car),
                        ""
                    )
                )
            }

            get("/add/{id}") {
                val id = call.parameters["id"]!!.toInt()
                val car:Car? = CarService().getCar(id)
                call.respond(
                    FreeMarkerContent(
                        "addCar.ftl",
                        mapOf("car" to car),
                        ""
                    )
                )
            }

            get("/my/cars"){
                val u = UserService().getUserByName(call.principal<UserIdPrincipal>()!!.name)
                call.respond(
                    FreeMarkerContent(
                        "mycar.ftl",
                        mapOf("cars" to CarService().getMyCars(u!!.id)),
                        ""
                    )
                )
            }

            post("/delete"){
                val params = call.receiveParameters()
                val idCar: Int = params["id"]!!.toInt()
                val u = UserService().getUserByName(call.principal<UserIdPrincipal>()!!.name)
                CarService().deleteCarFromUser(u!!.id,idCar)

                call.respond(
                    FreeMarkerContent(
                        "addCar.ftl",
                        mapOf("error" to "Usunięto","car" to CarService().getCar(idCar)),
                        ""
                    )
                )
            }



            get("/delete/{id}") {
                val id = call.parameters["id"]!!.toInt()
                val car:Car? = CarService().getCar(id)
                call.respond(
                    FreeMarkerContent(
                        "deleteCar.ftl",
                        mapOf("car" to car),
                        ""
                    )
                )
            }

            post("/add") {
                val params = call.receiveParameters()
                val idCar: Int = params["id"]!!.toInt()
                val u = UserService().getUserByName(call.principal<UserIdPrincipal>()!!.name)
                CarService().addCar(idCar,u!!.id)
                call.respond(
                    FreeMarkerContent(
                        "addCar.ftl",
                        mapOf("error" to "Dodano","car" to CarService().getCar(idCar)),
                        ""
                    )
                )
            }



            post("/details") {
                val params = call.receiveParameters()
                val pass: String = params["password"].toString()
                val oldPass: String = params["oldPassword"].toString()
                val user = UserService().getUserByName(call.principal<UserIdPrincipal>()!!.name)
                var error: String = ""
                try {
                    if (BCrypt.checkpw(oldPass, user!!.password)) {
                        if (pass.isNotEmpty()) {
                            error=validate(pass)
                            if(error.equals("OK")) {
                                val temp: User =
                                    User(user.id, user.name, BCrypt.hashpw(pass, BCrypt.gensalt()), user.address)
                                UserService().updateUser(temp)
                            }

                        }
                    } else error = "Złe hasło"
                } catch (e: Exception) {
                    error = "Coś poszło źle"
                }

                call.respond(
                    FreeMarkerContent(
                        "userDetails.ftl",
                        mapOf("error" to error, "name" to user!!.name, "adres" to user!!.address),
                        ""
                    )
                )
            }
        }
    }
}


fun initDataBase() {
    val hikariConfig = HikariConfig("/hikari.properties")
    val hikariDataSource = HikariDataSource(hikariConfig)
    Database.connect(hikariDataSource)
    transaction {
        create(Users)
        create(Cars)
        create(CarUsers)
    }
}

data class IndexData(val items: List<Int>)

fun validate(pass: String): String {
    val patternLetters = ".*[A-Z]+.*".toRegex()
    val specialCharacters = """.*[!@#$%^&*()_\\+=\\[\\]{}'\\"<>,\\.?|/]+.*""".toRegex()
    return if (pass.length < 5) {
        "Hasło za krótkie"
    }else if (!patternLetters.matches(pass)) {
        "Brak wielkich liter"
    }else if(!specialCharacters.matches(pass)){
        "Brak znaków specjalnych liter"
    }else
        "OK"

}




