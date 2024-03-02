package club.pisquad.uiharu.qqbot.websocket

import club.pisquad.uiharu.qqbot.api.QQBotApi
import club.pisquad.uiharu.qqbot.websocket.dto.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.util.logging.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

        setHeartbeatTimer(data.d!!.heartBeatInterval, session)
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
            when (data.op) {
                OpCode.DISPATCH.value -> EventHandler.handle(data.t, data.d!!)
            }
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

    private suspend fun setHeartbeatTimer(period: Long, session: DefaultClientWebSocketSession) {
        LOGGER.debug("Setting websocket heartbeat timer")
        //TODO: Cancel Timer before schedule a new one
        heartBeatTimer.schedule(delay = period, period = period) {
            runBlocking {
                LOGGER.info("Sending heartbeat")
                session.sendSerialized(PayloadBase(op = OpCode.HEARTBEAT.value, d = latestSerialNumber))
                //NOTE: We cannot receive and handle response here,
                // there will be conflicts with other places which are listening to the incoming messages
            }
        }
    }
}

fun Application.QQBotWebsocket() {
    launch { QQBotWebsocket.start() }
}