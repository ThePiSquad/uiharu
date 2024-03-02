package club.pisquad.uiharu.qqbot.websocket

import club.pisquad.uiharu.qqbot.api.QQBotApi
import club.pisquad.uiharu.qqbot.websocket.schemas.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.util.logging.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.util.*
import kotlin.concurrent.schedule
import kotlin.system.exitProcess

internal val LOGGER = KtorSimpleLogger("club.pisquad.uiharu.qqbot.websocket.QQBotWebsocket")

object QQBotWebsocket {
    private lateinit var gatewayUrl: String;
    private var heartBeatTimer: Timer = Timer()
    private var client: HttpClient = HttpClient(CIO) {
        install(WebSockets)
    }

    //TODO: use middleware for automatically trace the value
    private var latestSerialNumber: Int? = null

    private suspend fun connect(session: DefaultClientWebSocketSession) {
        // We already send a message to the server, just handle the response
        val data = session.receiveDeserialized<PayloadBase<ConnectResponseData>>()
        if (data.op != OpCode.HELLO.value) {
            LOGGER.error("Connect to websocket failed with response $data")
            exitProcess(-1)
        }
        latestSerialNumber = data.s
    }

    private suspend fun identify(session: DefaultClientWebSocketSession) {
        session.sendSerialized(
            PayloadBase(
                op = OpCode.IDENTIFY.value,
                d = IdentifyRequestData("QQBot " + QQBotApi.accessToken, intents = INTENT_VALUE)
            )
        )
        val data = session.receiveDeserialized<PayloadBase<IdentifyResponseData>>()
        latestSerialNumber = data.s

    }

    private suspend fun listen(session: DefaultClientWebSocketSession) {
        while (true) {
            val data = session.receiveDeserialized<PayloadBase<JsonElement>>()
            println("new incoming message $data")
        }
    }


    suspend fun start() {
        gatewayUrl = QQBotApi.getWebsocketGateway();
        LOGGER.debug("Connecting to websocket using gatewayUrl $gatewayUrl")
        client = HttpClient(CIO) {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json { ignoreUnknownKeys = true })
            }

        }
        client.webSocket({
            url.takeFrom(gatewayUrl)
        }) {
            connect(this)
            identify(this)
            listen(this)
        }
    }

    suspend fun setHeartbeatTimer(period: Long) {
        LOGGER.debug("Setting websocket heartbeat timer")
        heartBeatTimer.cancel()
        heartBeatTimer.schedule(delay = 0, period = period) {}
    }

}

fun Application.QQBotWebsocket() {
    launch { QQBotWebsocket.start() }
}