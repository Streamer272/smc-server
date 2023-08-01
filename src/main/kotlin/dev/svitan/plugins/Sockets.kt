package dev.svitan.plugins

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    var clients = arrayOf<WebSocketServerSession>()

    routing {
        webSocketRaw("/ws") {
            clients += this

            for (frame in incoming) {
                if (frame is Frame.Close) {
                    clients.drop(clients.indexOf(this))
                    break
                }

                if (frame is Frame.Text) {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    clients.forEach { if (it != this) it.outgoing.send(Frame.Text("someone said: $text")) }
                }
            }
        }
    }
}
