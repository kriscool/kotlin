package com.example

import com.example.com.example.WidgetModel
import com.example.dao.Events
import com.example.dao.Users
import com.example.service.UserService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import freemarker.cache.*
import io.ktor.freemarker.*
import io.ktor.features.*
import io.ktor.auth.*
import io.ktor.sessions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

//@Suppress("unused") // Referenced in application.conf
//@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(Sessions) {
        cookie<MySession>("SESSION")
    }

    install(Authentication) {
        form("login") {
            userParamName = "username"
            passwordParamName = "password"
            challenge = FormAuthChallenge.Unauthorized
            validate { credentials -> if (credentials.name  == "char" ) UserIdPrincipal(credentials.name) else null }
        }
    }
    Database.connect(hikari())
    transaction {
        create(Events)
        create(Users)
        UserService().registerUser("USER", "USER", "USER")
    }

    routing {
        get("/") {
            val session = call.sessions.get<MySession>()
            if (session != null) {
                call.respond(FreeMarkerContent("index.ftl", mapOf("log" to 1), ""))
            } else {
                call.respond(FreeMarkerContent("index.ftl", mapOf("log" to 0), "e"))
            }
        }
        route("/login") {
            get {
                call.respond(FreeMarkerContent("login.ftl", null))
            }
            authenticate("login") {
                post {
                    val principal = call.principal<UserIdPrincipal>()
                    call.sessions.set(MySession(principal!!.name))
                    call.respondRedirect("/", permanent = false)
                }
            }
        }
        get("/users"){
            val user = UserService().getUserFromName("USER")
            call.respond(FreeMarkerContent("users.ftl", mapOf("wid" to user!!.password), "e"))
        }
        get("/logout") {
                call.sessions.clear<MySession>()
                call.respondRedirect("/", permanent = false)
        }

        get("/html-dsl") {
            call.respondHtml {
                body {
                    h1 { +"HTML" }
                    ul {
                        for (n in 1..10) {
                            li { +"$n" }
                        }
                    }
                }
            }
        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }

        get("/html-freemarker") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(listOf(1, 2, 3))), ""))
        }

        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }

        }
    }
}

fun getWidgets(id: Int): WidgetModel{

    return transaction {
        Widgets.select {
            (Widgets.id eq id)
        }.map { WidgetModel(it[Widgets.id],it[Widgets.name],it[Widgets.quantity],it[Widgets.dateCreated]) }
            .first()
    }
}

private fun hikari(): HikariDataSource {
    val config = HikariConfig()
    config.driverClassName = "org.postgresql.ds.PGSimpleDataSource"
    config.jdbcUrl = "jdbc:postgresql://localhost:5432/kotlin"
    config.username="postgres"
    config.password="postgres"
    config.maximumPoolSize = 3
    config.isAutoCommit = false
    config.validate()
    return HikariDataSource(config)
}

object Widgets : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val quantity = integer("quantity")
    val dateCreated = long("dateCreated")
}

data class IndexData(val items: List<Int>)
data class MySession(val username: String)
class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
