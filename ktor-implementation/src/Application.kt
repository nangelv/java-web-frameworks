package ktor

import VideoFileManager
import VideoServiceController
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import java.io.Serializable

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) { json() }
    // very easy to track calls
    install(CallLogging)
    val controller = VideoServiceController(VideoFileManager.get())
    // There's probably more Kotlin/KTOR idiomatic way to write that
    routing {
        route("/video") {
            get { call.respond(controller.getAll())  }
            post { controller.create(call) }
            get("{id}/data") { controller.getOne(call) }
            post("{id}/data") { controller.upload(call) }
        }
    }
}
